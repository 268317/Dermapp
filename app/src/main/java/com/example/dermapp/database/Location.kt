package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

data class Location(
    @get:PropertyName("locationId") @set:PropertyName("locationId") var locationId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") var doctorId: String = "",
    @get:PropertyName("fullAddress") @set:PropertyName("fullAddress") var fullAddress: String = ""
)
