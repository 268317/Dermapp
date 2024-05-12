package com.example.dermapp.database

interface PatientFirestoreInterface {
    suspend fun addPatient(patient: Patient)
    suspend fun updatePatient(pesel: String, updatedPatient: Patient)
    suspend fun deletePatient(pesel: String)
    suspend fun getPatient(pesel: String): Patient?
}