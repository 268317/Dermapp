package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

open class Conversation(
    @get:PropertyName("conversationId") @set:PropertyName("conversationId") var conversationId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") var doctorId: String = "",
    @get:PropertyName("patientId") @set:PropertyName("patientId") var patientId: String = "",
    @get:PropertyName("lastMessage") @set:PropertyName("lastMessage") var lastMessage: String = "",
    @get:PropertyName("lastMessageTimestamp") @set:PropertyName("lastMessageTimestamp") var lastMessageTimestamp: com.google.firebase.Timestamp? = null
) {
    constructor() : this("", "", "", "", null)
}


