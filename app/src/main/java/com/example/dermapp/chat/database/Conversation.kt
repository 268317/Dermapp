package com.example.dermapp.chat.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Locale

open class Conversation(
    @get:PropertyName("conversationId") @set:PropertyName("conversationId") open var conversationId: String = "",
    @get:PropertyName("lastMessageId") @set:PropertyName("lastMessageId") open var lastMessageId: String = "",
    @get:PropertyName("participants") @set:PropertyName("participants") open var participants: List<String> = listOf()
) {
    constructor() : this("", "", listOf())

    /**
     * Retrieves the ID of the friend (other participant) in the conversation.
     *
     * @param currentUserId The ID of the current user.
     * @return The ID of the friend.
     */
    fun getFriendId(currentUserId: String): String? {
        return participants.find { it != currentUserId }
    }

    /**
     * Retrieves the profile photo of the friend (requires a Firestore query).
     *
     * @param currentUserId The ID of the current user.
     * @param callback Callback to return the result asynchronously.
     */
    fun getFriendProfilePhoto(currentUserId: String, callback: (String?) -> Unit) {
        val friendId = getFriendId(currentUserId)
        if (friendId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(friendId)
                .get()
                .addOnSuccessListener { document ->
                    callback(document.getString("profilePhoto"))
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    callback(null)
                }
        } else {
            callback(null)
        }
    }

    /**
     * Retrieves the username of the friend (requires a Firestore query).
     *
     * @param currentUserId The ID of the current user.
     * @param callback Callback to return the result asynchronously.
     */
    fun getFriendUsername(currentUserId: String, callback: (String?) -> Unit) {
        val friendId = getFriendId(currentUserId)
        if (friendId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(friendId)
                .get()
                .addOnSuccessListener { document ->
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    callback("$firstName $lastName".trim())
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    callback(null)
                }
        } else {
            callback(null)
        }
    }

    /**
     * Retrieves the timestamp of the last message in the desired format (e.g., Nov 16, 15:15).
     *
     * @param callback Callback to return the formatted date string asynchronously.
     */
    fun getLastMessageTimestamp(callback: (String?) -> Unit) {
        if (lastMessageId.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("messages")
                .document(lastMessageId)
                .get()
                .addOnSuccessListener { document ->
                    val timestamp = document.getTimestamp("timestamp")
                    if (timestamp != null) {
                        val date = timestamp.toDate()
                        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.ENGLISH)
                        callback(formatter.format(date))
                    } else {
                        callback(null)
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    callback(null)
                }
        } else {
            callback(null)
        }
    }

    /**
     * Retrieves the text of the last message (requires a Firestore query).
     *
     * @param callback Callback to return the result asynchronously.
     */
    fun getLastMessageText(callback: (String?) -> Unit) {
        if (lastMessageId.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("messages")
                .document(lastMessageId)
                .get()
                .addOnSuccessListener { document ->
                    callback(document.getString("messageText"))
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    callback(null)
                }
        } else {
            callback(null)
        }
    }
}