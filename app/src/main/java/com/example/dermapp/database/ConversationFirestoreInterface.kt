package com.example.dermapp.database

interface ConversationFirestoreInterface {
    fun addConversation(conversation: Conversation)
    fun updateConversation(conversationId: String, updatedConversation: Map<String, Any>)
    fun deleteConversation(conversationId: String)
    fun getConversation(conversationId: String)
}