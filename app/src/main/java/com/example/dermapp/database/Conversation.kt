package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName
import com.google.type.DateTime
import java.util.Date

open class Conversation(
    @get:PropertyName("conversationId") @set:PropertyName("conversationId") open var conversationId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") open var doctorId: String = "",
    @get:PropertyName("patientPesel") @set:PropertyName("patientPesel") open var patientPesel: String = "",
    @get:PropertyName("startTime") @set:PropertyName("startTime") open var startTime: DateTime,
    @get:PropertyName("endTime") @set:PropertyName("endTime") open var endTime: DateTime,
)