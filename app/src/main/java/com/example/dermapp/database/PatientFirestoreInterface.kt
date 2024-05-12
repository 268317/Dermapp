package com.example.dermapp.database

interface PatientFirestoreInterface {
    fun addPatient(patient: Patient)
    fun updatePatient(patientId: String, updatedPatient: Map<String, Any>)
    fun deletePatient(patientId: String)
    fun getPatient(patientId: String)
}