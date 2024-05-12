package com.example.dermapp.database

interface AppUserFirestoreInterface {
        suspend fun addAppUser(appUser: AppUser)
        suspend fun updateAppUser(appUserId: String, updatedAppUser: AppUser)
        suspend fun deleteAppUser(appUserId: String)
        suspend fun getAppUser(appUserId: String): AppUser?
    }