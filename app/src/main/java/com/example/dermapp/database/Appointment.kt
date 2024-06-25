package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName
import java.util.Date

/**
 * Data class representing an appointment in the dermatology application.
 *
 * @property appointmentId Unique identifier for the appointment.
 * @property doctorId Unique identifier for the doctor associated with the appointment.
 * @property patientId Unique identifier for the patient associated with the appointment.
 * @property datetime Date and time of the appointment.
 * @property localization Location where the appointment will take place.
 * @property diagnosis Diagnosis given during the appointment.
 * @property recommendations Recommendations provided during the appointment.
 */
data class Appointment(
    @get:PropertyName("appointmentId") @set:PropertyName("appointmentId") var appointmentId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") var doctorId: String = "",
    @get:PropertyName("patientId") @set:PropertyName("patientId") var patientId: String? = "",
    @get:PropertyName("datetime") @set:PropertyName("datetime") var datetime: Date? = null,
    @get:PropertyName("localization") @set:PropertyName("localization") var localization: String = "",
    @get:PropertyName("diagnosis") @set:PropertyName("diagnosis") var diagnosis: String = "",
    @get:PropertyName("recommendations") @set:PropertyName("recommendations") var recommendations: String = ""
)
