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
    @Query("SELECT * FROM donors ORDER BY name ASC")
    fun getAllDonors(): Flow<List<Donor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonor(donor: Donor): Long

    @Update
    suspend fun updateDonor(donor: Donor)

    @Delete
    suspend fun deleteDonor(donor: Donor)

    @Query("SELECT * FROM donors WHERE id = :id")
    suspend fun getDonorById(id: Int): Donor?
}
