package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

/**
 * Data class representing a doctor in the dermatology application.
 *
 * This class extends the [AppUser] class, inheriting common user attributes and adding
 * doctor-specific properties such as a unique doctor ID. It represents all relevant
 * details about a doctor in the system, enabling functionality such as authentication,
 * profile management, and communication.
 *
 * @property appUserId Unique identifier inherited from [AppUser]. Represents the doctor's general user ID.
 * @property email Email address of the doctor. Used for login and communication purposes.
 * @property password Password of the doctor. Secured and used for authentication.
 * @property firstName First name of the doctor. Personal identification detail.
 * @property lastName Last name of the doctor. Personal identification detail.
 * @property address Address of the doctor. Used for correspondence or record purposes.
 * @property phone Phone number of the doctor. Enables direct communication with the doctor.
 * @property birthDate Date of birth of the doctor. Recorded for identification and age validation.
 * @property doctorId Unique identifier specific to the doctor. Used for distinguishing doctors in the system.
 * @property role Role of the user, indicating that the user is a doctor in the system.
 * @property profilePhoto URL or path to the doctor's profile photo. Used for displaying the doctor's image in the application.
 * @property isOnline Boolean flag indicating the doctor's current online status. Useful for real-time availability tracking.
 */
data class Doctor(
    @get:PropertyName("appUserId") @set:PropertyName("appUserId") override var appUserId: String = "",
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
