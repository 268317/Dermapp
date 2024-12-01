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
import com.example.dermapp.chat.notifications.network.RetrofitInstance
import com.example.dermapp.chat.notifications.entity.NotificationData
import com.example.dermapp.chat.notifications.entity.PushNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessagesActivityPat : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: ImageView

    private val messageList = mutableListOf<Message>()
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserId by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    private var conversationId: String? = null
    private var doctorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_new_message)

        val backHeader = findViewById<LinearLayout>(R.id.header_chat)
        backButton = backHeader.findViewById(R.id.chatBackBtn)
        backButton.setOnClickListener {
            val intent = Intent(this, ChatsActivityPat::class.java)
            startActivity(intent)
        }

        conversationId = intent.getStringExtra("conversationId")
        doctorId = intent.getStringExtra("doctorId")

        Log.d("MessagesActivityPat", "Received conversationId: $conversationId")
        Log.d("MessagesActivityPat", "Received doctorId: $doctorId")

        if (conversationId == null) {
            Log.e("MessagesActivityPat", "conversationId is null!")
            return
        }

        recyclerView = findViewById(R.id.messagesRecyclerViewPat)
        messageInput = findViewById(R.id.editTextMessagePat)
        sendButton = findViewById(R.id.sendBtnPat)

        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessagesAdapter(this, messageList)
        recyclerView.adapter = messageAdapter

        val headerName: TextView = findViewById(R.id.chatUserNameDoc)
        val headerStatus: TextView = findViewById(R.id.chatUserStatusDoc)
        val headerProfileImage: ImageView = findViewById(R.id.chatImageViewUserDoc)

        val name = intent.getStringExtra("doctorName")
        val status = intent.getStringExtra("doctorStatus")
        val profilePhoto = intent.getStringExtra("doctorProfilePhoto")

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
                    Log.e("MessagesActivityPat", "Error fetching messages", e)
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
            Log.e("MessagesActivityPat", "Message text is empty")
            return
        }

        if (conversationId == null || doctorId == null) {
            Log.e("MessagesActivityPat", "Conversation ID or Doctor ID is null")
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
                Log.e("MessagesActivityPat", "Error checking conversation existence", e)
            }
    }

    private fun saveMessageAndUpdateConversation(messageText: String) {
        val senderProfileImage = intent.getStringExtra("patientProfilePhoto")
            ?: "https://example.com/black_circle_account.png"

        val message = hashMapOf(
            "messageId" to firestore.collection("messages").document().id,
            "conversationId" to conversationId,
            "senderId" to currentUserId,
            "receiverId" to doctorId,
            "messageText" to messageText,
            "timestamp" to FieldValue.serverTimestamp(),
            "isRead" to false,
            "senderProfileImage" to senderProfileImage
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
                        Log.d("MessagesActivityPat", "Conversation updated successfully.")
                        sendPushNotification(doctorId!!, "Nowa wiadomość", messageText)
                    }
                    .addOnFailureListener { e ->
                        Log.e("MessagesActivityPat", "Error updating conversation", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivityPat", "Error sending message", e)
            }
    }

    private fun createConversationAndSaveMessage(messageText: String) {
        val conversationData = hashMapOf(
            "conversationId" to conversationId,
            "lastMessage" to messageText,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "participants" to listOf(currentUserId, doctorId)
        )

        firestore.collection("conversation")
            .document(conversationId!!)
            .set(conversationData)
            .addOnSuccessListener {
                saveMessageAndUpdateConversation(messageText)
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivityPat", "Error creating conversation", e)
            }
    }

    private fun sendPushNotification(receiverToken: String, title: String, message: String) {
        val notificationData = NotificationData(title, message)
        val pushNotification = PushNotification(notificationData, receiverToken)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(pushNotification)
                if (response.isSuccessful) {
                    Log.d("MessagesActivityPat", "Push notification sent successfully")
                } else {
                    Log.e("MessagesActivityPat", "Error sending push notification: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("MessagesActivityPat", "Exception while sending push notification", e)
            }
        }
    }
}
