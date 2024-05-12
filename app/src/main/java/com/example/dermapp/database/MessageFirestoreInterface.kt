package com.example.dermapp.database

interface MessageFirestoreInterface {
    fun addMessage(message: Message)
    fun updateMessage(messageId: String, updatedMessage: Map<String, Any>)
    fun deleteMessage(messageId: String)
    fun getMessage(messageId: String)
}