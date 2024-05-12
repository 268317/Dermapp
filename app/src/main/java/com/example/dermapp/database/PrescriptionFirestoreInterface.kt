package com.example.dermapp.database

interface PrescriptionFirestoreInterface {
    suspend fun addPrescription(prescription: Prescription)
    suspend fun updatePrescription(prescriptionId: String, updatedPrescription: Prescription)
    suspend fun deletePrescription(prescriptionId: String)
    suspend fun getPrescription(prescriptionId: String):Prescription?
}