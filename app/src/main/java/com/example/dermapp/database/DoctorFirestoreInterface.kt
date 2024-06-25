package com.example.dermapp.database

/**
 * Interface for handling Firestore operations related to doctors in the dermatology application.
 */
interface DoctorFirestoreInterface {
    /**
     * Adds a new doctor to Firestore.
     *
     * @param doctor The doctor to be added.
     */
    suspend fun addDoctor(doctor: Doctor)

    /**
     * Updates an existing doctor in Firestore.
     *
     * @param doctorId The unique identifier of the doctor to be updated.
     * @param updatedDoctor The updated doctor data.
     */
    suspend fun updateDoctor(doctorId: String, updatedDoctor: Doctor)

    /**
     * Deletes a doctor from Firestore.
     *
     * @param doctorId The unique identifier of the doctor to be deleted.
     */
    suspend fun deleteDoctor(doctorId: String)

    /**
     * Retrieves a doctor from Firestore.
     *
     * @param doctorId The unique identifier of the doctor to be retrieved.
     * @return The retrieved doctor, or null if no doctor is found.
     */
    suspend fun getDoctor(doctorId: String): Doctor?
}
