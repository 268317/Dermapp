package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName
import com.google.type.DateTime
import java.util.Date

open class Appointment(
    @get:PropertyName("appointmentId") @set:PropertyName("appointmentId") open var appointmentId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") open var doctorId: String = "",
    @get:PropertyName("patientPesel") @set:PropertyName("patientPesel") open var patientPesel: String = "",
    @get:PropertyName("date") @set:PropertyName("date") open var appointmentDate: DateTime,
    @get:PropertyName("diagnosis") @set:PropertyName("diagnosis") open var diagnosis: String = "",
    @get:PropertyName("recommendations") @set:PropertyName("recommendations") open var recommendations: String = "",
    )