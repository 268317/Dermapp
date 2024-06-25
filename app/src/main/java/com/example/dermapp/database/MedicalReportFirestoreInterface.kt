package com.example.dermapp.database

/**
 * Interface defining Firestore operations for managing medical reports.
 */
interface MedicalReportFirestoreInterface {
    /**
     * Adds a new medical report to Firestore.
     *
     * @param medicalReport The medical report object to be added.
     */
    suspend fun addMedicalReport(medicalReport: MedicalReport)

    /**
     * Updates an existing medical report in Firestore.
     *
     * @param medicalReportId The ID of the medical report to update.
     * @param updatedMedicalReport The updated medical report object.
     */
    suspend fun updateMedicalReport(medicalReportId: String, updatedMedicalReport: MedicalRecord)

    /**
     * Deletes a medical report from Firestore.
     *
     * @param medicalReportId The ID of the medical report to delete.
     */
    suspend fun deleteMedicalReport(medicalReportId: String)

    /**
     * Retrieves a medical report from Firestore based on its ID.
     *
     * @param medicalReportId The ID of the medical report to retrieve.
     * @return The retrieved medical report object, or null if not found.
     */
    suspend fun getMedicalReport(medicalReportId: String): MedicalReport?
}
