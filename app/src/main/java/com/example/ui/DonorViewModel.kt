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

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DonorRepository(db.donorDao)
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
        notes: String
    ) {
        viewModelScope.launch {
            val donor = Donor(
                id = id,
                name = name,
                batch = batch,
                bloodGroup = bloodGroup,
                lastDonationDateMillis = lastDonationDateMillis,
                phone = phone,
                notes = notes
            )

            if (id == 0) {
                repository.insertDonor(donor)
            } else {
                repository.updateDonor(donor)
            }
        }
    }

    fun deleteDonor(donor: Donor) {
        viewModelScope.launch {
            repository.deleteDonor(donor)
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
