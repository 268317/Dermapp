package com.example.dermapp.database

interface MedicalReportFirestoreInterface {
    fun addMedicalReport(medicalReport: MedicalReport)
    fun updateMedicalReport(medicalReportId: String, updatedMedicalReport: Map<String, Any>)
    fun deleteMedicalReport(medicalReportId: String)
    fun getMedicalReport(medicalReportId: String)
}