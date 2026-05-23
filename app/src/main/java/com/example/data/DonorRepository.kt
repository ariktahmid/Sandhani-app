package com.example.data

import kotlinx.coroutines.flow.Flow

class DonorRepository(private val donorDao: DonorDao) {
    val allDonors: Flow<List<Donor>> = donorDao.getAllDonors()

    suspend fun getAllDonorsSync(): List<Donor> = donorDao.getAllDonorsSync()

    suspend fun insertDonor(donor: Donor): Long = donorDao.insertDonor(
        donor.copy(lastModifiedMillis = System.currentTimeMillis())
    )

    suspend fun insertDonors(donors: List<Donor>) = donorDao.insertDonors(donors)

    suspend fun updateDonor(donor: Donor) = donorDao.updateDonor(
        donor.copy(lastModifiedMillis = System.currentTimeMillis())
    )

    suspend fun deleteDonor(donor: Donor) {
        val softDeleted = donor.copy(isDeleted = true, lastModifiedMillis = System.currentTimeMillis())
        donorDao.updateDonor(softDeleted)
    }

    suspend fun getDonorById(id: Int): Donor? = donorDao.getDonorById(id)
}
