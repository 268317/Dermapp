package com.example.dermapp.database

interface AppUserFirestoreInterface {
        fun addUser(user: AppUser)
        fun updateUser(userId: String, updatedUser: Map<String, Any>)
        fun deleteUser(userId: String)
        fun getUser(userId: String)
    }