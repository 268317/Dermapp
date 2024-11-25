package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

open class Message(
    @get:PropertyName("messageId") @set:PropertyName("messageId") open var messageId: String = "",
    @get:PropertyName("conversationId") @set:PropertyName("conversationId") open var conversationId: String = "",
    @get:PropertyName("senderId") @set:PropertyName("senderId") open var senderId: String = "",
    @get:PropertyName("receiverId") @set:PropertyName("receiverId") open var receiverId: String = "",
    @get:PropertyName("messageText") @set:PropertyName("messageText") open var messageText: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") open var timestamp: com.google.firebase.Timestamp? = null,
    @get:PropertyName("isRead") @set:PropertyName("isRead") open var isRead: Boolean = false
) {
    constructor() : this("", "", "", "", "", null, false)
}

