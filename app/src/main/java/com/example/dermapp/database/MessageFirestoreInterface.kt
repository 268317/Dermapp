package com.example.dermapp.database

import Message

/**
 * Interface defining Firestore operations for managing messages.
 */
interface MessageFirestoreInterface {
    /**
     * Adds a new message to Firestore.
     *
     * @param message The message object to be added.
     */
    suspend fun addMessage(message: Message)

    /**
     * Updates an existing message in Firestore.
     *
     * @param messageId The ID of the message to update.
     * @param updatedMessage The updated message object.
     */
    suspend fun updateMessage(messageId: String, updatedMessage: Message)

    /**
     * Deletes a message from Firestore.
     *
     * @param messageId The ID of the message to delete.
     */
    suspend fun deleteMessage(messageId: String)

    /**
     * Retrieves a message from Firestore based on its ID.
     *
     * @param messageId The ID of the message to retrieve.
     * @return The retrieved message object, or null if not found.
     */
    suspend fun getMessage(messageId: String): Message?
}
