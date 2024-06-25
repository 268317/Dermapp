package com.example.dermapp.database

/**
 * Interface defining Firestore operations for interacting with Patient data.
 */
interface PatientFirestoreInterface {
    /**
     * Adds a new patient to Firestore.
     *
     * @param patient The patient object to be added.
     */
    suspend fun addPatient(patient: Patient)

    /**
     * Updates an existing patient in Firestore.
     *
     * @param pesel The PESEL (national identification number) of the patient to update.
     * @param updatedPatient The updated patient object.
     */
    suspend fun updatePatient(pesel: String, updatedPatient: Patient)

    /**
     * Deletes a patient from Firestore.
     *
     * @param pesel The PESEL (national identification number) of the patient to delete.
     */
    suspend fun deletePatient(pesel: String)

    /**
     * Retrieves a patient from Firestore based on the PESEL.
     *
     * @param pesel The PESEL (national identification number) of the patient to retrieve.
     * @return The retrieved patient object, or null if not found.
     */
    suspend fun getPatient(pesel: String): Patient?
}
