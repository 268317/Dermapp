package com.example.dermapp.database

interface MessageFirestoreInterface {
    suspend fun addMessage(message: Message)
    suspend fun updateMessage(messageId: String, updatedMessage: Message)
    suspend fun deleteMessage(messageId: String)
    suspend fun getMessage(messageId: String): Message?
}