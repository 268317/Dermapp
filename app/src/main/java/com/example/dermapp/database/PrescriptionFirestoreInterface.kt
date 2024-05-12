package com.example.dermapp.database

interface PrescriptionFirestoreInterface {
    fun addPrescription(prescription: Prescription)
    fun updatePrescription(prescriptionId: String, updatedPrescription: Map<String, Any>)
    fun deletePrescription(prescriptionId: String)
    fun getPrescription(prescriptionId: String)
}