package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

/**
 * Open class representing a user in the dermatology application.
 *
 * @property appUserId Unique identifier for the user.
 * @property email Email address of the user.
 * @property password Password of the user.
 * @property firstName First name of the user.
 * @property lastName Last name of the user.
 * @property address Address of the user.
 * @property phone Phone number of the user.
 * @property birthDate Birth date of the user.
 * @property role Role of the user (e.g., doctor, patient).
 * @property profilePhoto
 */
open class AppUser(
    @get:PropertyName("appUserId") @set:PropertyName("appUserId") open var appUserId: String = "",
    @get:PropertyName("email") @set:PropertyName("email") open var email: String = "",
    @get:PropertyName("password") @set:PropertyName("password") open var password: String = "",
    @get:PropertyName("firstName") @set:PropertyName("firstName") open var firstName: String = "",
    @get:PropertyName("lastName") @set:PropertyName("lastName") open var lastName: String = "",
    @get:PropertyName("address") @set:PropertyName("address") open var address: String = "",
    @get:PropertyName("phone") @set:PropertyName("phone") open var phone: String = "",
    @get:PropertyName("birthDate") @set:PropertyName("birthDate") open var birthDate: String = "",
    @get:PropertyName("role") @set:PropertyName("role") open var role: String = "",
    @get:PropertyName("profilePhoto") @set:PropertyName("profilePhoto") open var profilePhoto: String = ""
){
    constructor() : this("", "", "", "", "", "",
        "", "", "", "")
}
