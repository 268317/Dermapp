package com.example.dermapp.chat.notifications.entity

data class PushNotification(
    val data: NotificationData, // Zawiera dane powiadomienia
    val to: String // Token odbiorcy
)
