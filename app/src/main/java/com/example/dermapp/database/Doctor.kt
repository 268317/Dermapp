package com.example.dermapp.database

import com.google.firebase.database.PropertyName

/**
 * Data class representing a doctor in the dermatology application.
 *
 * @property doctorId Unique identifier for the doctor.
 * @property appUserId Unique identifier inherited from AppUser.
 * @property email Email address of the doctor.
 * @property password Password of the doctor.
 * @property firstName First name of the doctor.
 * @property lastName Last name of the doctor.
 * @property address Address of the doctor.
 * @property phone Phone number of the doctor.
 * @property birthDate Date of birth of the doctor.
 */
data class Doctor(
    @get:PropertyName("userId") @set:PropertyName("userId") override var appUserId: String = "",
    @get:PropertyName("email") @set:PropertyName("email") override var email: String = "",
    @get:PropertyName("password") @set:PropertyName("password") override var password: String = "",
    @get:PropertyName("firstName") @set:PropertyName("firstName") override var firstName: String = "",
    @get:PropertyName("lastName") @set:PropertyName("lastName") override var lastName: String = "",
    @get:PropertyName("address") @set:PropertyName("address") override var address: String = "",
    @get:PropertyName("phone") @set:PropertyName("phone") override var phone: String = "",
    @get:PropertyName("dateOfBirth") @set:PropertyName("dateOfBirth") override var birthDate: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") var doctorId: String = "",
    @get:PropertyName("role") @set:PropertyName("role") override var role: String = "",
    @get:PropertyName("profilePhoto") @set:PropertyName("profilePhoto") override var profilePhoto: String = "",
    @get:PropertyName("isOnline") @set:PropertyName("isOnline") override var isOnline: Boolean = false
) : AppUser(appUserId, email, password, firstName, lastName, address, phone, birthDate, role, profilePhoto, isOnline)
