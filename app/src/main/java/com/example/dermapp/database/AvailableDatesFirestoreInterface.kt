package com.example.dermapp.database

/**
 * Interface for handling Firestore operations related to available dates for appointments.
 */
interface AvailableDatesFirestoreInterface {
    /**
     * Adds a new available date to Firestore.
     *
     * @param availableDates The available date to be added.
     */
    suspend fun addAvailableDate(availableDates: AvailableDates)

    /**
     * Updates an existing available date in Firestore.
     *
     * @param availableDateId The unique identifier of the available date to be updated.
     * @param availableDates The updated available date data.
     */
    suspend fun updateAvailableDate(availableDateId: String, availableDates: AvailableDates)

    /**
     * Deletes an available date from Firestore.
     *
     * @param availableDateId The unique identifier of the available date to be deleted.
     */
    suspend fun deleteAvailableDate(availableDateId: String)

    /**
     * Retrieves an available date from Firestore.
     *
     * @param availableDateId The unique identifier of the available date to be retrieved.
     * @return The retrieved available date, or null if no available date is found.
     */
    suspend fun getAvailableDate(availableDateId: String): AvailableDates?
}
