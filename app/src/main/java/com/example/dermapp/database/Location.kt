package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

/**
 * Data class representing a location in the Firestore database.
 *
 * @property locationId The ID of the location.
 * @property doctorId The ID of the doctor associated with the location.
 * @property fullAddress The full address of the location.
 */
data class Location(
    @get:PropertyName("locationId") @set:PropertyName("locationId") var locationId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") var doctorId: String = "",
    @get:PropertyName("fullAddress") @set:PropertyName("fullAddress") var fullAddress: String = ""
)
