package com.example.dermapp.chat.notifications

import android.content.Context
import com.example.dermapp.R
import com.google.auth.oauth2.GoogleCredentials

object AuthManager {
    fun getBearerToken(context: Context): String {
        try {
            val inputStream = context.resources.openRawResource(R.raw.firebase_service_account)
            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            credentials.refreshIfExpired()
            return credentials.accessToken.tokenValue
        } catch (e: Exception) {
            throw IllegalStateException("Failed to get Bearer Token: ${e.message}", e)
        }
    }
}
