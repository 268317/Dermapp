package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName
import com.google.type.DateTime

/**
 * Open class representing a conversation between a doctor and a patient in the dermatology application.
 *
 * @property conversationId Unique identifier for the conversation.
 * @property doctorId Unique identifier for the doctor involved in the conversation.
 * @property patientPesel PESEL of the patient involved in the conversation.
 * @property startTime Date and time when the conversation started.
 * @property endTime Date and time when the conversation ended.
 */
open class Conversation(
    @get:PropertyName("conversationId") @set:PropertyName("conversationId") open var conversationId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") open var doctorId: String = "",
    @get:PropertyName("patientPesel") @set:PropertyName("patientPesel") open var patientPesel: String = "",
    @get:PropertyName("startTime") @set:PropertyName("startTime") open var startTime: DateTime,
    @get:PropertyName("endTime") @set:PropertyName("endTime") open var endTime: DateTime,
)
