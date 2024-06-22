package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class Appointment(
    @get:PropertyName("appointmentId") @set:PropertyName("appointmentId") var appointmentId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") var doctorId: String = "",
    @get:PropertyName("patientPesel") @set:PropertyName("patientPesel") var patientPesel: String = "",
    @get:PropertyName("datetime") @set:PropertyName("date") var appointmentDate: Date? = null,
    @get:PropertyName("localization") @set:PropertyName("localization") var localization: String = "",
    @get:PropertyName("diagnosis") @set:PropertyName("diagnosis") var diagnosis: String = "",
    @get:PropertyName("recommendations") @set:PropertyName("recommendations") var recommendations: String = ""
)
