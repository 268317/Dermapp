package com.example.dermapp.chat.notifications.network

import com.example.dermapp.chat.notifications.Constants
import com.example.dermapp.chat.notifications.entity.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {
    // Interfejs do wysyłania powiadomień przez FCM
    @Headers("Authorization: key=${Constants.SERVER_KEY}", "Content-Type: ${Constants.CONTENT_TYPE}")

    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}
