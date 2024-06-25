package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

/**
 * Data class representing a medical record in the Firestore database.
 *
 * @property medicalRecordId The ID of the medical record.
 * @property doctorId The ID of the doctor associated with the medical record.
 * @property patientPesel The PESEL (Personal Identification Number) of the patient associated with the medical record.
 * @property medicalRecordDate The date of the medical record.
 * @property type The type of the medical record.
 * @property attachment The attachment (e.g., file path) associated with the medical record.
 */
open class MedicalRecord(
    @get:PropertyName("medicalRecordId") @set:PropertyName("medicalRecordId") open var medicalRecordId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") open var doctorId: String = "",
    @get:PropertyName("patientPesel") @set:PropertyName("patientPesel") open var patientPesel: String = "",
    @get:PropertyName("date") @set:PropertyName("date") open var medicalRecordDate: String,
    @get:PropertyName("type") @set:PropertyName("type") open var type: String = "",
    @get:PropertyName("attachment") @set:PropertyName("attachment") open var attachment: String = ""
)
