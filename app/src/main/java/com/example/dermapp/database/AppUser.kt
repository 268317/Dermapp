package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

open class AppUser(
    @get:PropertyName("appUserId") @set:PropertyName("appUserId") open var appUserId: String = "",
    @get:PropertyName("email") @set:PropertyName("email") open var email: String = "",
    @get:PropertyName("password") @set:PropertyName("password") open var password: String = "",
    @get:PropertyName("firstName") @set:PropertyName("firstName") open var firstName: String = "",
    @get:PropertyName("lastName") @set:PropertyName("lastName") open var lastName: String = "",
    @get:PropertyName("birthDate") @set:PropertyName("birthDate") open var birthDate: String = "",
    @get:PropertyName("role") @set:PropertyName("role") open var role: String = "")
