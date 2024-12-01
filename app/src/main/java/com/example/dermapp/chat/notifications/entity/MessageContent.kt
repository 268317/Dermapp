package com.example.dermapp.chat.notifications.entity

data class MessageContent(
    val token: String,
    val notification: NotificationContent,
    val data: Map<String, String> // Jeśli potrzebujesz danych dodatkowych
)