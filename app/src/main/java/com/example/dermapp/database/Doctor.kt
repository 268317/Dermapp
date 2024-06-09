package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

data class Doctor(
    @get:PropertyName("userId") @set:PropertyName("userId") override var appUserId: String = "",
    @get:PropertyName("email") @set:PropertyName("email") override var email: String = "",
    @get:PropertyName("password") @set:PropertyName("password") override var password: String = "",
    @get:PropertyName("firstName") @set:PropertyName("firstName") override var firstName: String = "",
    @get:PropertyName("lastName") @set:PropertyName("lastName") override var lastName: String = "",
    @get:PropertyName("address") @set:PropertyName("address") override var address: String = "",
    @get:PropertyName("phone") @set:PropertyName("phone") override var phone: String = "",
    @get:PropertyName("dateOfBirth") @set:PropertyName("dateOfBirth") override var birthDate: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") var doctorId: String = ""
    ) : AppUser(appUserId, email, password, firstName, lastName, address, phone, birthDate)
