package com.example.dermapp.database

interface ConversationFirestoreInterface {
    suspend fun addConversation(conversation: Conversation)
    suspend fun updateConversation(conversationId: String, updatedConversation: Conversation)
    suspend fun deleteConversation(conversationId: String)
    suspend fun getConversation(conversationId: String): Conversation?
}