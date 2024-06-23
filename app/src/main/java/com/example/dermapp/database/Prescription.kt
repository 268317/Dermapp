package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class Prescription(
    @get:PropertyName("prescriptionId") @set:PropertyName("prescriptionId") open var prescriptionId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") open var doctorId: String = "",
    @get:PropertyName("patientId") @set:PropertyName("patientId") open var patientId: String = "",
    @get:PropertyName("date") @set:PropertyName("date") open var date: Date,
    @get:PropertyName("prescriptionText") @set:PropertyName("prescriptionText") open var prescriptionText: String = ""
) {
    // Konstruktor bezargumentowy
    constructor() : this("", "", "", Date(), "")
}
