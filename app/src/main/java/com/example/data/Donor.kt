package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "donors")
data class Donor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val batch: String,
    val bloodGroup: String, // A+, A-, B+, B-, O+, O-, AB+, AB-
    val lastDonationDateMillis: Long, // Epoch millis
    val phone: String = "",
    val notes: String = "",
    val uuid: String = UUID.randomUUID().toString(),
    val lastModifiedMillis: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
