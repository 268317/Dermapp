package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName
import java.util.Date

/**
 * Data class representing a prescription stored in Firestore.
 * @property prescriptionId The ID of the prescription.
 * @property doctorId The ID of the doctor who issued the prescription.
 * @property patientId The ID of the patient for whom the prescription is issued.
 * @property date The date when the prescription was issued.
 * @property prescriptionText The textual content of the prescription.
 */
data class Prescription(
    @get:PropertyName("prescriptionId") @set:PropertyName("prescriptionId") open var prescriptionId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") open var doctorId: String = "",
    @get:PropertyName("patientId") @set:PropertyName("patientId") open var patientId: String = "",
    @get:PropertyName("date") @set:PropertyName("date") open var date: Date,
    @get:PropertyName("prescriptionText") @set:PropertyName("prescriptionText") open var prescriptionText: String = ""
) {
    // No-argument constructor
    constructor() : this("", "", "", Date(), "")
}
