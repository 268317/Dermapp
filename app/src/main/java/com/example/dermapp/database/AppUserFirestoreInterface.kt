package com.example.dermapp.database

interface AppUserFirestoreInterface {
    suspend fun addAppUser(userMail: String, appUser: AppUser)

    suspend fun getAppUser(userMail: String): AppUser?

    suspend fun updateAppUser(userMail: String, updatedAppUser: AppUser)

    suspend fun deleteAppUser(userMail: String)
}