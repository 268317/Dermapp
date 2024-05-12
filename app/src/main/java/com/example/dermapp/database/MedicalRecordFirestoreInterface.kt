package com.example.dermapp.database

interface MedicalRecordFirestoreInterface {
    suspend fun addMedicalRecord(medicalRecord: MedicalRecord)
    suspend fun updateMedicalRecord(medicalRecordId: String, updatedMedicalRecord: MedicalRecord)
    suspend fun deleteMedicalRecord(medicalRecordId: String)
    suspend fun getMedicalRecord(medicalRecordId: String): MedicalRecord?
}