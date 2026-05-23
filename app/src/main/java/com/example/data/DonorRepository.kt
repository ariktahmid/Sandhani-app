package com.example.data

import kotlinx.coroutines.flow.Flow

class DonorRepository(private val donorDao: DonorDao) {
    val allDonors: Flow<List<Donor>> = donorDao.getAllDonors()

    suspend fun insertDonor(donor: Donor): Long = donorDao.insertDonor(donor)

    suspend fun updateDonor(donor: Donor) = donorDao.updateDonor(donor)

    suspend fun deleteDonor(donor: Donor) = donorDao.deleteDonor(donor)

    suspend fun getDonorById(id: Int): Donor? = donorDao.getDonorById(id)
}
