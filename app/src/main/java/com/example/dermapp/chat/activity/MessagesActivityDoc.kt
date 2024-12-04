package com.example.dermapp.chat.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

class MessagesActivityDoc : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: android.widget.Button
    private lateinit var backButton: ImageView

    private val messageList = mutableListOf<Message>()
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val functions by lazy { FirebaseFunctions.getInstance() }
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

        val headerName: TextView = findViewById(R.id.chatUserNameDoc)
        val headerStatus: TextView = findViewById(R.id.chatUserStatusDoc)
        val headerProfileImage: ImageView = findViewById(R.id.chatImageViewUserDoc)

        val name = intent.getStringExtra("patientName")
        val status = intent.getStringExtra("patientStatus")
        val profilePhoto = intent.getStringExtra("patientProfilePhoto")
        messageAdapter = MessagesAdapter(this, messageList, profilePhoto)
        recyclerView.adapter = messageAdapter

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

                    markMessagesAsRead()

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
                    .addOnFailureListener { e ->
                        Log.e("MessagesActivityDoc", "Error updating conversation", e)
                    }

                sendNotificationToReceiver(messageText)
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

    private fun sendNotificationToReceiver(messageText: String) {
        firestore.collection("users")
            .document(patientId!!)
            .get()
            .addOnSuccessListener { document ->
                val deviceToken = document.getString("deviceToken")
                if (deviceToken != null) {
                    val notificationData = hashMapOf(
                        "to" to deviceToken,
                        "notification" to hashMapOf(
                            "title" to "Nowa wiadomość",
                            "body" to messageText
                        )
                    )


                    functions.getHttpsCallable("sendNotification")
                        .call(notificationData)
                        .addOnSuccessListener {
                            Log.d("MessagesActivityDoc", "Notification sent successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("MessagesActivityDoc", "Failed to send notification", e)
                        }
                } else {
                    Log.e("MessagesActivityDoc", "Device token not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivityDoc", "Failed to fetch user data", e)
            }
    }

    private fun markMessagesAsRead() {
        if (conversationId == null) return

        firestore.collection("messages")
            .whereEqualTo("conversationId", conversationId)
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshots ->
                for (document in snapshots.documents) {
                    firestore.collection("messages")
                        .document(document.id)
                        .update("isRead", true)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivity", "Error marking messages as read", e)
            }
    }

}



//package com.example.dermapp.chat.activity
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.NotificationCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.dermapp.R
//import com.example.dermapp.chat.adapter.MessagesAdapter
//import com.example.dermapp.chat.database.Message
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FieldValue
//import com.google.firebase.firestore.FirebaseFirestore
//
//class MessagesActivityDoc : AppCompatActivity() {
//
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var messageAdapter: MessagesAdapter
//    private lateinit var messageInput: EditText
//    private lateinit var sendButton: Button
//    private lateinit var backButton: ImageView
//
//    private val messageList = mutableListOf<Message>()
//    private val firestore by lazy { FirebaseFirestore.getInstance() }
//    private val currentUserId by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
//
//    private var conversationId: String? = null
//    private var patientId: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.chat_activity_new_message)
//
//        val backHeader = findViewById<LinearLayout>(R.id.header_chat)
//        backButton = backHeader.findViewById(R.id.chatBackBtn)
//        backButton.setOnClickListener {
//            val intent = Intent(this, ChatsActivityDoc::class.java)
//            startActivity(intent)
//        }
//
//        conversationId = intent.getStringExtra("conversationId")
//        patientId = intent.getStringExtra("patientId")
//
//        recyclerView = findViewById(R.id.messagesRecyclerViewPat)
//        messageInput = findViewById(R.id.editTextMessagePat)
//        sendButton = findViewById(R.id.sendBtnPat)
//
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        messageAdapter = MessagesAdapter(this, messageList)
//        recyclerView.adapter = messageAdapter
//
//        val headerName: TextView = findViewById(R.id.chatUserNameDoc)
//        val headerStatus: TextView = findViewById(R.id.chatUserStatusDoc)
//        val headerProfileImage: ImageView = findViewById(R.id.chatImageViewUserDoc)
//
//        val name = intent.getStringExtra("patientName")
//        val status = intent.getStringExtra("patientStatus")
//        val profilePhoto = intent.getStringExtra("patientProfilePhoto")
//
//        headerName.text = name ?: "Unknown"
//        headerStatus.text = status ?: ""
//        profilePhoto?.let {
//            Glide.with(this)
//                .load(it)
//                .placeholder(R.drawable.black_account_circle)
//                .error(R.drawable.black_account_circle)
//                .circleCrop()
//                .into(headerProfileImage)
//        }
//
//        fetchMessages()
//
//        sendButton.setOnClickListener {
//            checkConversationAndSendMessage()
//        }
//
//        createNotificationChannel()
//    }
//
//    private fun fetchMessages() {
//        if (conversationId == null) return
//
//        firestore.collection("messages")
//            .whereEqualTo("conversationId", conversationId)
//            .orderBy("timestamp")
//            .addSnapshotListener { snapshots, e ->
//                if (e != null) {
//                    Log.e("MessagesActivityDoc", "Error fetching messages", e)
//                    return@addSnapshotListener
//                }
//
//                snapshots?.let {
//                    messageList.clear()
//                    for (document in snapshots.documents) {
//                        val message = document.toObject(Message::class.java)
//                        message?.let { messageList.add(it) }
//                    }
//                    messageAdapter.notifyDataSetChanged()
//                    recyclerView.scrollToPosition(messageList.size - 1)
//                }
//            }
//    }
//
//    private fun checkConversationAndSendMessage() {
//        val messageText = messageInput.text.toString().trim()
//        if (messageText.isEmpty()) {
//            Log.e("MessagesActivityDoc", "Message text is empty")
//            return
//        }
//
//        if (conversationId == null || patientId == null) {
//            Log.e("MessagesActivityDoc", "Conversation ID or Patient ID is null")
//            return
//        }
//
//        firestore.collection("conversation")
//            .document(conversationId!!)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    saveMessageAndUpdateConversation(messageText)
//                } else {
//                    createConversationAndSaveMessage(messageText)
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("MessagesActivityDoc", "Error checking conversation existence", e)
//            }
//    }
//
//    private fun saveMessageAndUpdateConversation(messageText: String) {
//        val message = hashMapOf(
//            "messageId" to firestore.collection("messages").document().id,
//            "conversationId" to conversationId,
//            "senderId" to currentUserId,
//            "receiverId" to patientId,
//            "messageText" to messageText,
//            "timestamp" to FieldValue.serverTimestamp(),
//            "isRead" to false
//        )
//
//        firestore.collection("messages")
//            .add(message)
//            .addOnSuccessListener {
//                messageInput.text.clear()
//                firestore.collection("conversation")
//                    .document(conversationId!!)
//                    .update(
//                        mapOf(
//                            "lastMessage" to messageText,
//                            "lastMessageTimestamp" to FieldValue.serverTimestamp()
//                        )
//                    )
//                    .addOnSuccessListener {
//                        sendNotificationToReceiver(messageText)
//                    }
//                    .addOnFailureListener { e ->
//                        Log.e("MessagesActivityDoc", "Error updating conversation", e)
//                    }
//            }
//            .addOnFailureListener { e ->
//                Log.e("MessagesActivityDoc", "Error sending message", e)
//            }
//    }
//
//    private fun createConversationAndSaveMessage(messageText: String) {
//        val conversationData = hashMapOf(
//            "conversationId" to conversationId,
//            "lastMessage" to messageText,
//            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
//            "participants" to listOf(currentUserId, patientId)
//        )
//
//        firestore.collection("conversation")
//            .document(conversationId!!)
//            .set(conversationData)
//            .addOnSuccessListener {
//                saveMessageAndUpdateConversation(messageText)
//            }
//            .addOnFailureListener { e ->
//                Log.e("MessagesActivityDoc", "Error creating conversation", e)
//            }
//    }
//
//    private fun sendNotificationToReceiver(messageText: String) {
//        val intent = Intent(this, MessagesActivityDoc::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notification = NotificationCompat.Builder(this, "messages_channel")
//            .setSmallIcon(R.drawable.logo_foreground)
//            .setContentTitle("New Message")
//            .setContentText(messageText)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .build()
//
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "messages_channel",
//                "Messages",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Notifications for new messages"
//            }
//            val manager = getSystemService(NotificationManager::class.java)
//            manager?.createNotificationChannel(channel)
//        }
//    }
//}
