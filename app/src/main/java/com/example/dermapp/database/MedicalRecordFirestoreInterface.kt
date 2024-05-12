package com.example.dermapp.database

interface MedicalRecordFirestoreInterface {
    fun addMedicalRecord(medicalRecord: MedicalRecord)
    fun updateMedicalRecord(medicalRecordId: String, updatedMedicalRecord: Map<String, Any>)
    fun deleteMedicalRecord(medicalRecordId: String)
    fun getMedicalRecord(medicalRecordId: String)
}