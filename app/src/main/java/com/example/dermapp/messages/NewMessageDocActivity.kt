package com.example.dermapp.messages

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.dermapp.R
import com.example.dermapp.database.AppUser
import com.example.dermapp.database.Message
import com.example.dermapp.messages.adapter.NewMessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

class NewMessageDocActivity : AppCompatActivity() {

    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var adapter: NewMessageAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private val messagesList = mutableListOf<Message>()

    private val currentDoctorId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var conversationId: String
    private lateinit var patientId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_new_message_doc)

        findViewById<ImageView>(R.id.chatBackBtn).setOnClickListener {
            startActivity(Intent(this, MessagesDocActivity::class.java))
        }

        conversationId = intent.getStringExtra("conversationId") ?: ""
        patientId = intent.getStringExtra("patientId") ?: ""

        messagesRecyclerView = findViewById(R.id.messagesRecyclerViewDoc)
        messageInput = findViewById(R.id.editTextMessageDoc)
        sendButton = findViewById(R.id.sendBtnDoc)

        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NewMessageAdapter(this, messagesList)
        messagesRecyclerView.adapter = adapter

        listenForMessages()
        setupHeader()
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageInput.text.clear()
            }
        }
    }

    private fun listenForMessages() {
        FirebaseFirestore.getInstance().collection("messages")
            .whereEqualTo("conversationId", conversationId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    e.printStackTrace()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val newMessages = snapshots.documents.mapNotNull { it.toObject(Message::class.java) }
                        .sortedBy { it.timestamp }
                    messagesList.clear()
                    messagesList.addAll(newMessages)
                    adapter.notifyDataSetChanged()
                    messagesRecyclerView.scrollToPosition(messagesList.size - 1)
                }
            }
    }

    private fun sendMessage(messageText: String) {
        val firestore = FirebaseFirestore.getInstance()
        val messageId = firestore.collection("messages").document().id

        val message = Message(
            messageId = messageId,
            conversationId = conversationId,
            senderId = currentDoctorId,
            receiverId = patientId,
            messageText = messageText,
            timestamp = com.google.firebase.Timestamp.now()
        )

        firestore.collection("messages").document(message.messageId).set(message).addOnSuccessListener {
            // Update the last message in the conversation
            firestore.collection("conversation").document(conversationId).update(
                mapOf(
                    "lastMessage" to messageText,
                    "lastMessageTimestamp" to message.timestamp
                )
            ).addOnSuccessListener {
                // Local notification for the sender
                showLocalNotification("Message Sent", "Your message was successfully sent.")

                // Notification for the receiver
                sendNotificationToReceiver(messageText)
            }.addOnFailureListener {
                showLocalNotification("Error", "Failed to update the conversation.")
            }
        }.addOnFailureListener { e ->
            showLocalNotification("Error", "Failed to send the message: ${e.message}")
        }
    }

    private fun sendNotificationToReceiver(messageText: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(patientId).get().addOnSuccessListener { document ->
            val fcmToken = document.getString("fcmToken")

            if (!fcmToken.isNullOrEmpty()) {
                val notificationData = mapOf(
                    "to" to fcmToken,
                    "notification" to mapOf(
                        "title" to "New Message",
                        "body" to messageText
                    )
                )

                sendNotificationRequest(notificationData)
            }
        }
    }

    private fun sendNotificationRequest(payload: Map<String, Any>) {
        val url = "https://fcm.googleapis.com/fcm/send"
        val requestQueue = Volley.newRequestQueue(this)
        val request = object : JsonObjectRequest(
            Request.Method.POST, url, JSONObject(payload),
            { response ->
                Log.d("Notification", "Notification sent: $response")
            },
            { error ->
                Log.e("Notification", "Error sending notification: ${error.message}")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = mutableMapOf<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "key=YOUR_SERVER_KEY"
                return headers
            }
        }
        requestQueue.add(request)
    }

    private fun showLocalNotification(title: String, message: String) {
        val channelId = "default_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun setupHeader() {
        val profileImageView = findViewById<ImageView>(R.id.chatImageViewUserDoc)
        val userNameTextView = findViewById<TextView>(R.id.chatUserNameDoc)
        val userStatusTextView = findViewById<TextView>(R.id.chatUserStatusDoc)

        FirebaseFirestore.getInstance().collection("users").document(patientId)
            .addSnapshotListener { document, exception ->
                if (exception != null) {
                    exception.printStackTrace()
                    return@addSnapshotListener
                }

                if (!isFinishing && !isDestroyed) {
                    val user = document?.toObject(AppUser::class.java)
                    if (user != null) {
                        userNameTextView.text = user.firstName
                        userStatusTextView.text = if (user.isOnline) "Online" else "Offline"

                        if (!user.profilePhoto.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(user.profilePhoto)
                                .placeholder(R.drawable.account_circle)
                                .into(profileImageView)
                        } else {
                            profileImageView.setImageResource(R.drawable.account_circle)
                        }
                    }
                }
            }
    }
}
