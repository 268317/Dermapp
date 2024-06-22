package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class AvailableDates(
    @get:PropertyName("availableDateId") @set:PropertyName("availableDateId") var availableDateId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") var doctorId: String = "",
    @get:PropertyName("datetime") @set:PropertyName("datetime") var datetime: Date = Date(),
    @get:PropertyName("locationId") @set:PropertyName("locationId") var locationId: String = "",
    @get:PropertyName("isAvailable") @set:PropertyName("isAvailable") var isAvailable: Boolean = true
)
