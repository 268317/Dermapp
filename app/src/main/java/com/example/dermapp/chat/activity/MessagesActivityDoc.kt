package com.example.dermapp.chat.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermapp.R
import com.example.dermapp.chat.adapter.MessagesAdapter
import com.example.dermapp.chat.database.Message
import com.example.dermapp.chat.notifications.entity.MessageContent
import com.example.dermapp.chat.notifications.entity.NotificationContent
import com.example.dermapp.chat.notifications.entity.PushNotificationRequest
import com.example.dermapp.chat.notifications.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessagesActivityDoc : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: ImageView

    private val messageList = mutableListOf<Message>()
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserId by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    private var conversationId: String? = null
    private var patientId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_new_message)

        val backHeader = findViewById<LinearLayout>(R.id.header_chat)
        backButton = backHeader.findViewById(R.id.chatBackBtn)
        backButton.setOnClickListener {
            val intent = Intent(this, ChatsActivityDoc::class.java)
            startActivity(intent)
        }

        conversationId = intent.getStringExtra("conversationId")
        patientId = intent.getStringExtra("patientId")

        recyclerView = findViewById(R.id.messagesRecyclerViewPat)
        messageInput = findViewById(R.id.editTextMessagePat)
        sendButton = findViewById(R.id.sendBtnPat)

        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessagesAdapter(this, messageList)
        recyclerView.adapter = messageAdapter

        val headerName: TextView = findViewById(R.id.chatUserNameDoc)
        val headerStatus: TextView = findViewById(R.id.chatUserStatusDoc)
        val headerProfileImage: ImageView = findViewById(R.id.chatImageViewUserDoc)

        val name = intent.getStringExtra("patientName")
        val status = intent.getStringExtra("patientStatus")
        val profilePhoto = intent.getStringExtra("patientProfilePhoto")

        headerName.text = name ?: "Unknown"
        headerStatus.text = status ?: ""
        profilePhoto?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.black_account_circle)
                .error(R.drawable.black_account_circle)
                .circleCrop()
                .into(headerProfileImage)
        }

        fetchMessages()

        sendButton.setOnClickListener {
            checkConversationAndSendMessage()
        }
    }

    private fun fetchMessages() {
        if (conversationId == null) return

        firestore.collection("messages")
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("MessagesActivityDoc", "Error fetching messages", e)
                    return@addSnapshotListener
                }

                snapshots?.let {
                    messageList.clear()
                    for (document in snapshots.documents) {
                        val message = document.toObject(Message::class.java)
                        message?.let { messageList.add(it) }
                    }
                    messageAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messageList.size - 1)
                }
            }
    }

    private fun checkConversationAndSendMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isEmpty()) {
            Log.e("MessagesActivityDoc", "Message text is empty")
            return
        }

        if (conversationId == null || patientId == null) {
            Log.e("MessagesActivityDoc", "Conversation ID or Patient ID is null")
            return
        }

        firestore.collection("conversation")
            .document(conversationId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    saveMessageAndUpdateConversation(messageText)
                } else {
                    createConversationAndSaveMessage(messageText)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivityDoc", "Error checking conversation existence", e)
            }
    }

    private fun saveMessageAndUpdateConversation(messageText: String) {
        val message = hashMapOf(
            "messageId" to firestore.collection("messages").document().id,
            "conversationId" to conversationId,
            "senderId" to currentUserId,
            "receiverId" to patientId,
            "messageText" to messageText,
            "timestamp" to FieldValue.serverTimestamp(),
            "isRead" to false
        )

        firestore.collection("messages")
            .add(message)
            .addOnSuccessListener {
                messageInput.text.clear()
                firestore.collection("conversation")
                    .document(conversationId!!)
                    .update(
                        mapOf(
                            "lastMessage" to messageText,
                            "lastMessageTimestamp" to FieldValue.serverTimestamp()
                        )
                    )
                    .addOnSuccessListener {
                        fetchFcmTokenAndSendNotification(patientId!!, "Nowa wiadomość", messageText)
                    }
                    .addOnFailureListener { e ->
                        Log.e("MessagesActivityDoc", "Error updating conversation", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivityDoc", "Error sending message", e)
            }
    }

    private fun createConversationAndSaveMessage(messageText: String) {
        val conversationData = hashMapOf(
            "conversationId" to conversationId,
            "lastMessage" to messageText,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "participants" to listOf(currentUserId, patientId)
        )

        firestore.collection("conversation")
            .document(conversationId!!)
            .set(conversationData)
            .addOnSuccessListener {
                saveMessageAndUpdateConversation(messageText)
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivityDoc", "Error creating conversation", e)
            }
    }

    private fun fetchFcmTokenAndSendNotification(userId: String, title: String, message: String) {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val receiverToken = document.getString("fcmToken")
                    if (!receiverToken.isNullOrEmpty()) {
                        sendPushNotification(receiverToken, title, message)
                    } else {
                        Log.e("MessagesActivityDoc", "No FCM token found for user: $userId")
                    }
                } else {
                    Log.e("MessagesActivityDoc", "User document not found for userId: $userId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivityDoc", "Error fetching user token: ${e.message}")
            }
    }

    private fun sendPushNotification(receiverToken: String, title: String, message: String) {
        val notificationRequest = PushNotificationRequest(
            message = MessageContent(
                token = receiverToken,
                notification = NotificationContent(
                    title = title,
                    body = message
                ),
                data = mapOf(
                    "key1" to "value1",
                    "key2" to "value2"
                )
            )
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notificationRequest)
                if (response.isSuccessful) {
                    Log.d("FCM", "Notification sent successfully")
                } else {
                    Log.e("FCM", "Error sending notification: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Exception sending notification", e)
            }
        }
    }
}
