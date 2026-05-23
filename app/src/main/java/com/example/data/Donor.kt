package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donors")
data class Donor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val batch: String,
    val bloodGroup: String, // A+, A-, B+, B-, O+, O-, AB+, AB-
    val lastDonationDateMillis: Long, // Epoch millis
    val phone: String = "",
    val notes: String = ""
)
