package com.example.dermapp.chat.database

import com.google.firebase.firestore.PropertyName
import com.google.firebase.Timestamp

/**
 * Represents a message in a chat conversation.
 *
 * @property messageId The unique identifier of the message.
 * @property conversationId The ID of the conversation this message belongs to.
 * @property senderId The ID of the user who sent the message.
 * @property receiverId The ID of the user who received the message.
 * @property messageText The text content of the message.
 * @property timestamp The timestamp when the message was sent.
 * @property isRead Indicates whether the message has been read by the receiver.
 * @property photoUrl The URL of a photo attached to the message, if any.
 */
open class Message(
    @get:PropertyName("messageId") @set:PropertyName("messageId") open var messageId: String = "",
    @get:PropertyName("conversationId") @set:PropertyName("conversationId") open var conversationId: String = "",
    @get:PropertyName("senderId") @set:PropertyName("senderId") open var senderId: String = "",
    @get:PropertyName("receiverId") @set:PropertyName("receiverId") open var receiverId: String = "",
    @get:PropertyName("messageText") @set:PropertyName("messageText") open var messageText: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") open var timestamp: Timestamp? = null,
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
