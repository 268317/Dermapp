package com.example.dermapp.database
interface AvailableDatesFirestoreInterface {
    suspend fun addAvailableDate(availableDates: AvailableDates)
    suspend fun updateAvailableDate(availableDateId: String, availableDates: AvailableDates)
    suspend fun deleteAvailableDate(availableDateId: String)
    suspend fun getAvailableDate(availableDateId: String): AvailableDates?
}