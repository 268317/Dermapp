package com.example.dermapp.database

import com.google.firebase.database.PropertyName

data class Patient (
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("last name") @set:PropertyName("last name") var lastName: String = "",
    @get:PropertyName("date of birthday") @set:PropertyName("date of birthday") var birthday: String = ""
)