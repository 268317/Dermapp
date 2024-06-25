package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

/**
 * Data class representing a message in the application.
 */
open class Message(
    @get:PropertyName("messageId") @set:PropertyName("messageId") open var messageId: String = "",
    // @get:PropertyName("conversationId") @set:PropertyName("conversationId") open var conversationId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") open var doctorId: String = "",
    @get:PropertyName("patientId") @set:PropertyName("patientId") open var patientId: String = "",
    // @get:PropertyName("date") @set:PropertyName("date") open var messageDate: Timestamp,
    @get:PropertyName("messageText") @set:PropertyName("messageText") open var messageText: String = "",
)