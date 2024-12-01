package com.example.dermapp.chat.notifications.network

import com.example.dermapp.chat.notifications.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: NotificationAPI by lazy {
        retrofit.create(NotificationAPI::class.java)
    }
}
