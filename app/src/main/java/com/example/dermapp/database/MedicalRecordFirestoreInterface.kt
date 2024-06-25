package com.example.dermapp.database

/**
 * Interface defining Firestore operations for managing medical records.
 */
interface MedicalRecordFirestoreInterface {
    /**
     * Adds a medical record to Firestore.
     *
     * @param medicalRecord The medical record to be added.
     */
    suspend fun addMedicalRecord(medicalRecord: MedicalRecord)

    /**
     * Updates a medical record in Firestore.
     *
     * @param medicalRecordId The ID of the medical record to update.
     * @param updatedMedicalRecord The updated medical record object.
     */
    suspend fun updateMedicalRecord(medicalRecordId: String, updatedMedicalRecord: MedicalRecord)

    /**
     * Deletes a medical record from Firestore.
     *
     * @param medicalRecordId The ID of the medical record to delete.
     */
    suspend fun deleteMedicalRecord(medicalRecordId: String)

    /**
     * Retrieves a medical record from Firestore.
     *
     * @param medicalRecordId The ID of the medical record to retrieve.
     * @return The retrieved medical record object, or null if not found.
     */
    suspend fun getMedicalRecord(medicalRecordId: String): MedicalRecord?
}
