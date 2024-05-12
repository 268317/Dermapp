package com.example.dermapp.database

interface MedicalReportFirestoreInterface {
    suspend fun addMedicalReport(medicalReport: MedicalReport)
    suspend fun updateMedicalReport(medicalReportId: String, updatedMedicalReport: MedicalRecord)
    suspend fun deleteMedicalReport(medicalReportId: String)
    suspend fun getMedicalReport(medicalReportId: String): MedicalReport?
}