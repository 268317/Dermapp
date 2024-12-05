package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName
import java.util.Date

/**
 * Data class representing an appointment in the dermatology application.
 *
 * This class is used to store and manage data related to dermatology appointments,
 * including information about the doctor, patient, date, location, and other relevant details.
 *
 * @property appointmentId Unique identifier for the appointment. This ID is used to distinguish appointments in the database.
 * @property doctorId Unique identifier for the doctor associated with the appointment. Links the appointment to a specific doctor.
 * @property patientId Unique identifier for the patient associated with the appointment. Links the appointment to a specific patient.
 * @property datetime Date and time of the appointment. This indicates when the appointment is scheduled to occur.
 * @property localization Location where the appointment will take place. Can represent a clinic or hospital address.
 * @property diagnosis Diagnosis provided during the appointment. Stores any medical conclusions drawn by the doctor.
 * @property recommendations Recommendations provided during the appointment. Includes any advice or next steps for the patient.
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
