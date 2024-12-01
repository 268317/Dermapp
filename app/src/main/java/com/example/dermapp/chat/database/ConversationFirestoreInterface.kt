package com.example.dermapp.chat.database

/**
 * Interface for handling Firestore operations related to conversations between doctors and patients.
 */
interface ConversationFirestoreInterface {
    /**
     * Adds a new conversation to Firestore.
     *
     * @param conversation The conversation to be added.
     */
    suspend fun addConversation(conversation: Conversation)

    /**
     * Updates an existing conversation in Firestore.
     *
     * @param conversationId The unique identifier of the conversation to be updated.
     * @param updatedConversation The updated conversation data.
     */
    suspend fun updateConversation(conversationId: String, updatedConversation: Conversation)

    /**
     * Deletes a conversation from Firestore.
     *
     * @param conversationId The unique identifier of the conversation to be deleted.
     */
    suspend fun deleteConversation(conversationId: String)

    /**
     * Retrieves a conversation from Firestore.
     *
     * @param conversationId The unique identifier of the conversation to be retrieved.
     * @return The retrieved conversation, or null if no conversation is found.
     */
    suspend fun getConversation(conversationId: String): Conversation?

    suspend fun getUserConversations(userId: String): List<Conversation>

}
