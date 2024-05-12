package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName
import com.google.type.DateTime
import java.util.Date

open class Message(
    @get:PropertyName("messageId") @set:PropertyName("prescriptionId") open var prescriptionId: String = "",
    @get:PropertyName("conversationId") @set:PropertyName("conversationId") open var conversationId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") open var doctorId: String = "",
    @get:PropertyName("patientPesel") @set:PropertyName("patientPesel") open var patientPesel: String = "",
    @get:PropertyName("date") @set:PropertyName("date") open var messageDate: DateTime,
    @get:PropertyName("messageText") @set:PropertyName("messageText") open var messageText: String = "",
)