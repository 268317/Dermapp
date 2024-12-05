package com.example.dermapp.chat.database

import com.google.firebase.firestore.PropertyName

open class Message(
    @get:PropertyName("messageId") @set:PropertyName("messageId") open var messageId: String = "",
    @get:PropertyName("conversationId") @set:PropertyName("conversationId") open var conversationId: String = "",
    @get:PropertyName("senderId") @set:PropertyName("senderId") open var senderId: String = "",
    @get:PropertyName("receiverId") @set:PropertyName("receiverId") open var receiverId: String = "",
    @get:PropertyName("messageText") @set:PropertyName("messageText") open var messageText: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") open var timestamp: com.google.firebase.Timestamp? = null,
    @get:PropertyName("isRead") @set:PropertyName("isRead") open var isRead: Boolean = false,
    @get:PropertyName("photoUrl") @set:PropertyName("photoUrl") open var photoUrl: String? = null,
) {
    constructor() : this("", "", "", "", "", null, false, null)

    /**
     * Checks if the message is sent by the current user.
     *
     * @param currentUserId The ID of the current user.
     * @return True if the current user is the sender, false otherwise.
     */
    fun isSender(currentUserId: String): Boolean = senderId == currentUserId

    /**
     * Checks if the message is unread for the current user.
     *
     * @param currentUserId The ID of the current user.
     * @return True if the message is unread and was sent to the current user.
     */
    fun isUnreadForCurrentUser(currentUserId: String): Boolean = !isRead && receiverId == currentUserId
}