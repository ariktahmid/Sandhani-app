package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DonorDao {
    @Query("SELECT * FROM donors WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllDonors(): Flow<List<Donor>>

    @Query("SELECT * FROM donors")
    suspend fun getAllDonorsSync(): List<Donor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonor(donor: Donor): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonors(donors: List<Donor>)

    @Update
    suspend fun updateDonor(donor: Donor)

    @Delete
    suspend fun deleteDonor(donor: Donor)

    @Query("SELECT * FROM donors WHERE id = :id")
    suspend fun getDonorById(id: Int): Donor?
}
