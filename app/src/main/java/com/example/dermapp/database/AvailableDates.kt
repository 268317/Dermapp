package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName
import java.util.Date

/**
 * Data class representing available dates for appointments in the dermatology application.
 *
 * @property availableDateId Unique identifier for the available date.
 * @property doctorId Unique identifier for the doctor associated with the available date.
 * @property datetime Date and time of the available slot.
 * @property locationId Unique identifier for the location of the available slot.
 * @property isAvailable Boolean indicating if the date is available for booking.
 */
data class AvailableDates(
    @get:PropertyName("availableDateId") @set:PropertyName("availableDateId") var availableDateId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") var doctorId: String = "",
    @get:PropertyName("datetime") @set:PropertyName("datetime") var datetime: Date = Date(),
    @get:PropertyName("locationId") @set:PropertyName("locationId") var locationId: String = "",
    @get:PropertyName("isAvailable") @set:PropertyName("isAvailable") var isAvailable: Boolean = true
)
