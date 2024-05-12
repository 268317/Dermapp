package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

data class Doctor(
    @get:PropertyName("userId") @set:PropertyName("userId") override var userId: String = "",
    @get:PropertyName("email") @set:PropertyName("email") override var email: String = "",
    @get:PropertyName("password") @set:PropertyName("password") override var password: String = "",
    @get:PropertyName("firstName") @set:PropertyName("firstName") override var firstName: String = "",
    @get:PropertyName("lastName") @set:PropertyName("lastName") override var lastName: String = "",
    @get:PropertyName("dateOfBirth") @set:PropertyName("dateOfBirth") override var birthDate: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") var doctorId: String = ""
    ) : AppUser(userId, email, password, firstName, lastName, birthDate)
