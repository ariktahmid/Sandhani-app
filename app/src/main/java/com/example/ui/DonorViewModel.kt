package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Donor
import com.example.data.DonorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DonorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DonorRepository

    // Sync status states
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _syncSuccess = MutableStateFlow<Boolean?>(null)
    val syncSuccess = _syncSuccess.asStateFlow()

    private val _lastSyncedMillis = MutableStateFlow<Long>(0L)
    val lastSyncedMillis = _lastSyncedMillis.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DonorRepository(db.donorDao)
        // Perform an automatic cloud synchronization on app startup
        performSync()
    }

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Filter by blood group
    private val _selectedBloodGroupFilter = MutableStateFlow<String?>(null)
    val selectedBloodGroupFilter = _selectedBloodGroupFilter.asStateFlow()

    // Sort by type: "Eligibility" (Eligible first), "Name" (A-Z), "Last Donation Date" (Newest first)
    private val _sortBy = MutableStateFlow("Eligibility")
    val sortBy = _sortBy.asStateFlow()

    // Get combined list matching search, filter and sort
    val donorsState: StateFlow<List<Donor>> = combine(
        repository.allDonors,
        _searchQuery,
        _selectedBloodGroupFilter,
        _sortBy
    ) { list, query, bgFilter, sort ->
        var filtered = list

        // Apply blood group filter (e.g. "O+")
        if (bgFilter != null) {
            filtered = filtered.filter { it.bloodGroup.equals(bgFilter, ignoreCase = true) }
        }

        // Apply search text query
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.batch.contains(query, ignoreCase = true) ||
                it.phone.contains(query, ignoreCase = true) ||
                it.bloodGroup.contains(query, ignoreCase = true)
            }
        }

        // Apply sorting
        when (sort) {
            "Name" -> filtered.sortedBy { it.name.lowercase() }
            "Last Donation Date" -> filtered.sortedByDescending { it.lastDonationDateMillis }
            "Eligibility" -> {
                // Eligible donors first, then sorted by last donation date (oldest first, i.e. most ready)
                filtered.sortedWith { d1, d2 ->
                    val el1 = isEligibleToDonate(d1.lastDonationDateMillis)
                    val el2 = isEligibleToDonate(d2.lastDonationDateMillis)
                    if (el1 && !el2) -1
                    else if (!el1 && el2) 1
                    else {
                        // If both are in the same eligibility bucket, oldest donation date first
                        d1.lastDonationDateMillis.compareTo(d2.lastDonationDateMillis)
                    }
                }
            }
            else -> filtered
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setBloodGroupFilter(bg: String?) {
        _selectedBloodGroupFilter.value = bg
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
    }

    fun saveDonor(
        id: Int,
        name: String,
        batch: String,
        bloodGroup: String,
        lastDonationDateMillis: Long,
        phone: String,
        notes: String,
        existingUuid: String? = null
    ) {
        viewModelScope.launch {
            val finalUuid = if (id != 0) {
                existingUuid ?: repository.getDonorById(id)?.uuid ?: java.util.UUID.randomUUID().toString()
            } else {
                java.util.UUID.randomUUID().toString()
            }

            val donor = Donor(
                id = id,
                name = name,
                batch = batch,
                bloodGroup = bloodGroup,
                lastDonationDateMillis = lastDonationDateMillis,
                phone = phone,
                notes = notes,
                uuid = finalUuid,
                lastModifiedMillis = System.currentTimeMillis(),
                isDeleted = false
            )

            if (id == 0) {
                repository.insertDonor(donor)
            } else {
                repository.updateDonor(donor)
            }
            // Real-time synchronization whenever changes are saved locally
            performSync()
        }
    }

    fun deleteDonor(donor: Donor) {
        viewModelScope.launch {
            repository.deleteDonor(donor)
            // Real-time synchronization whenever a donor is deleted
            performSync()
        }
    }

    fun performSync(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            if (_isSyncing.value) return@launch
            _isSyncing.value = true
            _syncSuccess.value = null
            try {
                // 1. Fetch current pool from Sandbox cloud key-value store
                val networkDonors = try {
                    com.example.data.SyncApi.instance.getDonors()
                } catch (e: retrofit2.HttpException) {
                    if (e.code() == 404) {
                        emptyList()
                    } else {
                        throw e
                    }
                } catch (e: Exception) {
                    emptyList()
                }

                // 2. Load all local records (active + soft-deleted)
                val localDonors = repository.getAllDonorsSync()

                // Merge and reconcile lists
                val mergedList = mutableListOf<Donor>()
                val localToUpsert = mutableListOf<Donor>()

                val networkMap = networkDonors.associateBy { it.uuid }
                val localMap = localDonors.associateBy { it.uuid }

                val allUuids = (networkMap.keys + localMap.keys).toSet()

                for (uuid in allUuids) {
                    val local = localMap[uuid]
                    val network = networkMap[uuid]

                    if (local != null && network != null) {
                        // Conflict resolution based on lastModifiedMillis
                        if (local.lastModifiedMillis >= network.lastModifiedMillis) {
                            mergedList.add(local)
                        } else {
                            val updated = network.copy(id = local.id)
                            mergedList.add(updated)
                            localToUpsert.add(updated)
                        }
                    } else if (local != null) {
                        mergedList.add(local)
                    } else if (network != null) {
                        if (!network.isDeleted) {
                            val newLocal = network.copy(id = 0)
                            mergedList.add(newLocal)
                            localToUpsert.add(newLocal)
                        } else {
                            mergedList.add(network)
                        }
                    }
                }

                // Sync updates to SQLite
                if (localToUpsert.isNotEmpty()) {
                    repository.insertDonors(localToUpsert)
                }

                // Handle deletions across nodes
                for (local in localDonors) {
                    val net = networkMap[local.uuid]
                    if (net != null && net.isDeleted && !local.isDeleted) {
                        repository.updateDonor(local.copy(isDeleted = true, lastModifiedMillis = net.lastModifiedMillis))
                    }
                }

                // 3. Upload combined, fully synchronized array back to sandbox
                val finalUploadList = repository.getAllDonorsSync()
                com.example.data.SyncApi.instance.saveDonors(finalUploadList)

                _lastSyncedMillis.value = System.currentTimeMillis()
                _syncSuccess.value = true
                _isSyncing.value = false
                onComplete(true, "App data synchronized with the shared network!")
            } catch (e: Exception) {
                e.printStackTrace()
                _syncSuccess.value = false
                _isSyncing.value = false
                onComplete(false, "Sync failed: ${e.localizedMessage}")
            }
        }
    }

    companion object {
        fun isEligibleToDonate(lastDonationMillis: Long): Boolean {
            val nextEligible = Calendar.getInstance().apply {
                timeInMillis = lastDonationMillis
                add(Calendar.MONTH, 4)
            }
            val today = Calendar.getInstance()
            return today.after(nextEligible) || today.equals(nextEligible)
        }

        fun getTimeRemainingString(lastDonationMillis: Long): String {
            val nextEligible = Calendar.getInstance().apply {
                timeInMillis = lastDonationMillis
                add(Calendar.MONTH, 4)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (today.after(nextEligible) || today.equals(nextEligible)) {
                return "Ready to Donate"
            }

            val diffMillis = nextEligible.timeInMillis - today.timeInMillis
            val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

            if (diffDays <= 0) {
                return "Ready to Donate"
            }

            val months = diffDays / 30
            val days = diffDays % 30

            return when {
                months > 0 && days > 0 -> "$months mo $days d left"
                months > 0 -> "$months month${if (months > 1) "s" else ""} left"
                else -> "$diffDays day${if (diffDays > 1) "s" else ""} left"
            }
        }

        fun formatDate(millis: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(millis))
        }
    }
}
