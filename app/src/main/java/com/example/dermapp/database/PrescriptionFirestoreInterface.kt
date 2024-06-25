package com.example.dermapp.database

/**
 * Interface defining operations for interacting with Firestore regarding prescriptions.
 */
interface PrescriptionFirestoreInterface {

    /**
     * Adds a new prescription to Firestore.
     * @param prescription The prescription object to be added.
     */
    suspend fun addPrescription(prescription: Prescription)

    /**
     * Updates an existing prescription in Firestore.
     * @param prescriptionId The ID of the prescription to update.
     * @param updatedPrescription The updated prescription object.
     */
    suspend fun updatePrescription(prescriptionId: String, updatedPrescription: Prescription)

    /**
     * Deletes a prescription from Firestore.
     * @param prescriptionId The ID of the prescription to delete.
     */
    suspend fun deletePrescription(prescriptionId: String)

    /**
     * Retrieves a prescription from Firestore based on its ID.
     * @param prescriptionId The ID of the prescription to retrieve.
     * @return The [Prescription] object if found, null otherwise.
     */
    suspend fun getPrescription(prescriptionId: String): Prescription?
}
