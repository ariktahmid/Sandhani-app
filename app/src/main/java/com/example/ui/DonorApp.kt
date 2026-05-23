package com.example.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Donor
import java.util.Calendar

// Theme colors specifically crafted for Sandhani Blood Donors (Elegant Dark Theme)
val DarkBg = Color(0xFF1C1B1F)         // Base screen background
val DarkSurface = Color(0xFF2B2930)    // High-contrast cards, dialogs
val LightPinkAccent = Color(0xFFF2B8B5) // Warm light crimson (Pink) for active elements
val DeepPinkText = Color(0xFF601410)   // Dark crimson text on Pink accents
val LavenderAccent = Color(0xFFD0BCFF) // Soft Lavender accent (Fabs, active states)
val DeepPurpleText = Color(0xFF381E72) // Text color on Lavender
val AccentSlate = Color(0xFF49454F)    // Dark search bar / button backgrounds
val TextActive = Color(0xFFE6E1E5)     // Core white/silver text
val TextMuted = Color(0xFFCAC4D0)      // Sub-label text
val TextSubtle = Color(0xFF938F99)     // Secondary field labels

// Status indicator colors
val EligibleGreen = Color(0xFFB3FFB3)  // Eligible "Ready to donate" green
val EligibleGreenBg = Color(0x1ADBFFDB)
val WarningRose = Color(0xFFF2B8B5)    // Non-eligible soft blood alert
val WarningRoseBg = Color(0x1AF2B8B5)

val DarkSurfaceSecondary = Color(0xFF211F26) // Bottom navigation level/accent container

// Legacy theme colors mapped to Elegant Dark variables for direct compatibility
val CrimsonPrimary = LavenderAccent
val CrimsonLight = DarkSurface
val CrimsonDark = DeepPurpleText
val SurfaceCream = DarkBg
val AccentGold = WarningRose
val SoftGreen = EligibleGreen
val SoftGreenBg = EligibleGreenBg
val SoftAmberBg = WarningRoseBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorApp(modifier: Modifier = Modifier, viewModel: DonorViewModel) {
    val context = LocalContext.current
    val donors by viewModel.donorsState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val bgFilter by viewModel.selectedBloodGroupFilter.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var donorToEdit by remember { mutableStateOf<Donor?>(null) }
    var donorToDelete by remember { mutableStateOf<Donor?>(null) }

    // Aggregate stats
    val totalDonors = donors.size
    val eligibleDonors = donors.count { DonorViewModel.isEligibleToDonate(it.lastDonationDateMillis) }
    val waitingDonors = totalDonors - eligibleDonors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LightPinkAccent,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "S",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = DeepPinkText
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Sandhani",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TextActive
                            )
                            Text(
                                text = "Blood Donation Network",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TextMuted
                            )
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AccentSlate),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = TextActive.copy(alpha = 0.7f),
                                    shape = CircleShape
                                )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg,
                    titleContentColor = TextActive
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    donorToEdit = null
                    showAddEditDialog = true
                },
                containerColor = LavenderAccent,
                contentColor = DeepPurpleText,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .testTag("add_donor_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Donor")
                    Text("Add Donor", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = DarkBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Stats summary panel
            StatsSection(
                totalCount = totalDonors,
                eligibleCount = eligibleDonors,
                waitingCount = waitingDonors
            )

            // Search, Filter & Sort
            FilterAndSortSection(
                searchQuery = searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) },
                selectedBloodGroup = bgFilter,
                onBloodGroupSelect = { viewModel.setBloodGroupFilter(it) },
                currentSort = sortBy,
                onSortChange = { viewModel.setSortBy(it) }
            )

            // Donor List
            if (donors.isEmpty()) {
                EmptyStateSection(
                    isFiltering = searchQuery.isNotEmpty() || bgFilter != null,
                    onClearFilters = {
                        viewModel.setSearchQuery("")
                        viewModel.setBloodGroupFilter(null)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(donors, key = { it.id }) { donor ->
                        DonorCard(
                            donor = donor,
                            onEdit = {
                                donorToEdit = donor
                                showAddEditDialog = true
                            },
                            onDelete = {
                                donorToDelete = donor
                            },
                            onCall = { phone ->
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$phone")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not launch dialer", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog Wrapper
    if (showAddEditDialog) {
        AddEditDonorDialog(
            donor = donorToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { name, batch, bloodGroup, dateMillis, phone, notes ->
                viewModel.saveDonor(
                    id = donorToEdit?.id ?: 0,
                    name = name,
                    batch = batch,
                    bloodGroup = bloodGroup,
                    lastDonationDateMillis = dateMillis,
                    phone = phone,
                    notes = notes
                )
                showAddEditDialog = false
                val msg = if (donorToEdit == null) "Donor saved successfully" else "Donor updated successfully"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    donorToDelete?.let { donor ->
        AlertDialog(
            onDismissRequest = { donorToDelete = null },
            title = { Text("Delete Donor?", fontWeight = FontWeight.Bold, color = WarningRose) },
            text = { Text("Are you sure you want to permanently delete donor ${donor.name} from the database?", color = TextMuted) },
            modifier = Modifier.testTag("delete_confirmation_dialog"),
            containerColor = DarkSurface,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDonor(donor)
                        donorToDelete = null
                        Toast.makeText(context, "Donor removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningRose, contentColor = DeepPinkText)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { donorToDelete = null }
                ) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun StatsSection(totalCount: Int, eligibleCount: Int, waitingCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, TextActive.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Total
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = totalCount.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextActive
                )
                Text(
                    text = "Total Donors",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            // Separator
            Spacer(modifier = Modifier.width(1.dp).height(32.dp).background(AccentSlate.copy(alpha = 0.5f)))

            // Eligible
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EligibleGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = eligibleCount.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = EligibleGreen
                    )
                }
                Text(
                    text = "Eligible Now",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            // Separator
            Spacer(modifier = Modifier.width(1.dp).height(32.dp).background(AccentSlate.copy(alpha = 0.5f)))

            // Waiting
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(WarningRose)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = waitingCount.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningRose
                    )
                }
                Text(
                    text = "Waiting Period",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterAndSortSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedBloodGroup: String?,
    onBloodGroupSelect: (String?) -> Unit,
    currentSort: String,
    onSortChange: (String) -> Unit
) {
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    var sortByExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search & Sort bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("search_bar"),
                placeholder = { Text("Search donors or batches...", fontSize = 14.sp, color = TextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextMuted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = AccentSlate,
                    unfocusedContainerColor = AccentSlate,
                    focusedTextColor = TextActive,
                    unfocusedTextColor = TextActive,
                    cursorColor = LavenderAccent
                )
            )

            // Sort Dropdown button (Pill avatar circle styled beautifully)
            Box {
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable { sortByExpanded = true }
                        .testTag("sort_button"),
                    color = LavenderAccent,
                    border = null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Sort Selection",
                            tint = DeepPurpleText,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = sortByExpanded,
                    onDismissRequest = { sortByExpanded = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    val sortOptions = listOf("Eligibility", "Name", "Last Donation Date")
                    sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentSort == option) LavenderAccent else TextActive
                                )
                            },
                            onClick = {
                                onSortChange(option)
                                sortByExpanded = false
                            },
                            trailingIcon = {
                                if (currentSort == option) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = LavenderAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // Horizontal scrolling blood group chips
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Filter Blood Group:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                if (selectedBloodGroup != null) {
                    Text(
                        text = "Clear filter",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LavenderAccent,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { onBloodGroupSelect(null) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                bloodGroups.forEach { bg ->
                    val isSelected = selectedBloodGroup == bg
                    val cardBg = if (isSelected) LightPinkAccent else DarkSurface
                    val textColor = if (isSelected) DeepPinkText else TextActive
                    val borderColor = if (isSelected) Color.Transparent else AccentSlate

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onBloodGroupSelect(if (isSelected) null else bg) }
                            .testTag("blood_chip_$bg"),
                        color = cardBg,
                        border = BorderStroke(1.dp, borderColor),
                    ) {
                        Text(
                            text = bg,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonorCard(
    donor: Donor,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: (String) -> Unit
) {
    val isEligible = DonorViewModel.isEligibleToDonate(donor.lastDonationDateMillis)
    val statusText = DonorViewModel.getTimeRemainingString(donor.lastDonationDateMillis)

    val progress = remember(donor.lastDonationDateMillis) {
        val days = ((System.currentTimeMillis() - donor.lastDonationDateMillis) / (1000L * 60 * 60 * 24)).toFloat()
        (days / 120f).coerceIn(0f, 1f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("donor_card_${donor.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, TextActive.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val stripeWidth = 4.dp.toPx()
                    drawRect(
                        color = if (isEligible) LightPinkAccent else LavenderAccent,
                        size = androidx.compose.ui.geometry.Size(stripeWidth, this.size.height)
                    )
                }
                .padding(start = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Main info row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Blood group circular/pill badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isEligible) LightPinkAccent else LavenderAccent,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = donor.bloodGroup,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEligible) DeepPinkText else DeepPurpleText
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Name & Batch
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = donor.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextActive,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Batch: ${donor.batch}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }

                    // Edit/Delete action buttons
                    Row {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.testTag("edit_donor_${donor.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit donor",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("delete_donor_${donor.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete donor",
                                tint = WarningRose.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Last donation info & eligibility
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceSecondary)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Last Donated",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSubtle
                        )
                        Text(
                            text = DonorViewModel.formatDate(donor.lastDonationDateMillis),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextActive
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isEligible) Icons.Default.Check else Icons.Default.Info,
                            contentDescription = "Status icon",
                            tint = if (isEligible) EligibleGreen else WarningRose,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isEligible) EligibleGreen else WarningRose
                            )
                        )
                    }
                }

                // Dynamic progress indicators from mockups
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(AccentSlate)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(if (isEligible) EligibleGreen else LavenderAccent)
                    )
                }

                // Contact / Notes section
                if (donor.phone.isNotEmpty() || donor.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (donor.phone.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onCall(donor.phone) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone icon",
                                    tint = LavenderAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = donor.phone,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = LavenderAccent
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (donor.notes.isNotEmpty()) {
                            Text(
                                text = donor.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false).padding(start = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateSection(isFiltering: Boolean, onClearFilters: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = AccentSlate
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "No results",
                        tint = WarningRose,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isFiltering) "No Donors Found" else "No Blood Donors Saved",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextActive
                )
                Text(
                    text = if (isFiltering) {
                        "Try clearing tags or changing your query keywords."
                    } else {
                        "Tap the \"Add Donor\" button below to register a new member in the database."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }

            if (isFiltering) {
                TextButton(onClick = onClearFilters) {
                    Text("Clear Search & Filters", color = LavenderAccent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditDonorDialog(
    donor: Donor?,
    onDismiss: () -> Unit,
    onSave: (name: String, batch: String, bloodGroup: String, dateMillis: Long, phone: String, notes: String) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(donor?.name ?: "") }
    var batch by remember { mutableStateOf(donor?.batch ?: "") }
    var bloodGroup by remember { mutableStateOf(donor?.bloodGroup ?: "A+") }
    var phone by remember { mutableStateOf(donor?.phone ?: "") }
    var notes by remember { mutableStateOf(donor?.notes ?: "") }

    var selectedDateMillis by remember {
        mutableStateOf(donor?.lastDonationDateMillis ?: System.currentTimeMillis())
    }

    // Checking of "Never Donated" which sets the donation date millisecond to highly far back in the past to make them eligible instantly!
    var neverDonated by remember {
        mutableStateOf(
            donor == null ||
            // Consider "never donated" if the date is set to epoch 0 or standard far back date in database
            donor.lastDonationDateMillis <= 1000L * 60 * 60 * 24 * 365
        )
    }

    if (neverDonated) {
        // Set to 1 year ago so they are 100% eligible
        val oneYearAgo = Calendar.getInstance().apply {
            add(Calendar.YEAR, -1)
        }
        selectedDateMillis = oneYearAgo.timeInMillis
    }

    // Simple validation states
    var nameError by remember { mutableStateOf(false) }
    var batchError by remember { mutableStateOf(false) }

    val datePickerCallback = remember {
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedDateMillis = calendar.timeInMillis
            neverDonated = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_edit_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            border = BorderStroke(1.dp, TextActive.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (donor == null) "Register Blood Donor" else "Edit Donor Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightPinkAccent
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Name text field
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = false
                            },
                            label = { Text("Donor Full Name") },
                            isError = nameError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("donor_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LavenderAccent,
                                unfocusedBorderColor = AccentSlate,
                                focusedLabelColor = LavenderAccent,
                                unfocusedLabelColor = TextMuted,
                                focusedTextColor = TextActive,
                                unfocusedTextColor = TextActive,
                                focusedContainerColor = DarkBg,
                                unfocusedContainerColor = DarkBg
                            )
                        )
                        if (nameError) {
                            Text(
                                "Name is required",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }

                    item {
                        // Batch text field
                        OutlinedTextField(
                            value = batch,
                            onValueChange = {
                                batch = it
                                batchError = false
                            },
                            label = { Text("College Batch (e.g. K-76)") },
                            isError = batchError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("donor_batch_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LavenderAccent,
                                unfocusedBorderColor = AccentSlate,
                                focusedLabelColor = LavenderAccent,
                                unfocusedLabelColor = TextMuted,
                                focusedTextColor = TextActive,
                                unfocusedTextColor = TextActive,
                                focusedContainerColor = DarkBg,
                                unfocusedContainerColor = DarkBg
                            )
                        )
                        if (batchError) {
                            Text(
                                "Batch is required",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }

                    item {
                        // Phone field
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("donor_phone_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LavenderAccent,
                                unfocusedBorderColor = AccentSlate,
                                focusedLabelColor = LavenderAccent,
                                unfocusedLabelColor = TextMuted,
                                focusedTextColor = TextActive,
                                unfocusedTextColor = TextActive,
                                focusedContainerColor = DarkBg,
                                unfocusedContainerColor = DarkBg
                            )
                        )
                    }

                    item {
                        // Blood group selection grid
                        Text(
                            text = "Blood Group",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val bgList = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            bgList.forEach { bg ->
                                val isSelected = bloodGroup == bg
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { bloodGroup = bg }
                                        .testTag("dialog_blood_chip_$bg"),
                                    color = if (isSelected) LightPinkAccent else DarkBg,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color.Transparent else AccentSlate
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = bg,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) DeepPinkText else TextActive
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        // Never Donated Checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { neverDonated = !neverDonated }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = neverDonated,
                                onCheckedChange = { neverDonated = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LavenderAccent,
                                    uncheckedColor = TextMuted,
                                    checkmarkColor = DeepPurpleText
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "First time or never donated (Eligible immediately!)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextActive
                            )
                        }
                    }

                    item {
                        if (!neverDonated) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Last Donation Date",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedButton(
                                onClick = {
                                    val calendar = Calendar.getInstance().apply {
                                        timeInMillis = selectedDateMillis
                                    }
                                    DatePickerDialog(
                                        context,
                                        datePickerCallback,
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("date_picker_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AccentSlate),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LavenderAccent)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Select Date"
                                    )
                                    Text(
                                        text = DonorViewModel.formatDate(selectedDateMillis),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Additional Notes text field
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Special Medical Notes / Details") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("donor_notes_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LavenderAccent,
                                unfocusedBorderColor = AccentSlate,
                                focusedLabelColor = LavenderAccent,
                                unfocusedLabelColor = TextMuted,
                                focusedTextColor = TextActive,
                                unfocusedTextColor = TextActive,
                                focusedContainerColor = DarkBg,
                                unfocusedContainerColor = DarkBg
                            )
                        )
                    }
                }

                // Actions footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) nameError = true
                            if (batch.isBlank()) batchError = true

                            if (!nameError && !batchError) {
                                onSave(
                                    name.trim(),
                                    batch.trim(),
                                    bloodGroup,
                                    selectedDateMillis,
                                    phone.trim(),
                                    notes.trim()
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent, contentColor = DeepPurpleText),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_donor_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
