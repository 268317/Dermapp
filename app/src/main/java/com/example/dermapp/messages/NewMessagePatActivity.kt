package com.example.dermapp.messages

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermapp.R
import com.example.dermapp.database.AppUser
import com.example.dermapp.database.Message
import com.example.dermapp.messages.adapter.NewMessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.util.Log

class NewMessagePatActivity : AppCompatActivity() {

    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var adapter: NewMessageAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private val messagesList = mutableListOf<Message>()

    private val currentPatientId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var conversationId: String
    private lateinit var doctorId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_new_message_pat)

        findViewById<ImageView>(R.id.chatBackBtn).setOnClickListener {
            startActivity(Intent(this, MessagesPatActivity::class.java))
        }

        conversationId = intent.getStringExtra("conversationId") ?: ""
        doctorId = intent.getStringExtra("doctorId") ?: ""

        messagesRecyclerView = findViewById(R.id.messagesRecyclerViewPat)
        messageInput = findViewById(R.id.editTextMessagePat)
        sendButton = findViewById(R.id.sendBtnPat)

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
            senderId = currentPatientId,
            receiverId = doctorId,
            messageText = messageText,
            timestamp = com.google.firebase.Timestamp.now()
        )

        // Zapisanie wiadomości w Firestore
        firestore.collection("messages").document(message.messageId).set(message).addOnSuccessListener {
            firestore.collection("conversation").document(conversationId).update(
                mapOf(
                    "lastMessage" to messageText,
                    "lastMessageTimestamp" to message.timestamp
                )
            ).addOnFailureListener {
                firestore.collection("conversation").document(conversationId).set(
                    mapOf(
                        "conversationId" to conversationId,
                        "doctorId" to doctorId,
                        "patientId" to currentPatientId,
                        "lastMessage" to messageText,
                        "lastMessageTimestamp" to message.timestamp
                    )
                )
            }

            // Wywołanie funkcji do wysyłania powiadomienia
            sendNotificationToReceiver(doctorId, messageText)
        }
    }

    private fun sendNotificationToReceiver(receiverId: String, messageText: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(receiverId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val fcmToken = document.getString("fcmToken") ?: return@addOnSuccessListener
                // Przygotowanie powiadomienia do wysłania
                val payload = hashMapOf(
                    "to" to fcmToken,
                    "notification" to mapOf(
                        "title" to "Nowa wiadomość",
                        "body" to messageText
                    )
                )
                sendNotificationRequest(payload)
            }
        }
    }

    private fun sendNotificationRequest(payload: Map<String, Any>) {
        val url = "https://fcm.googleapis.com/fcm/send"
        val requestQueue = Volley.newRequestQueue(this)
        val request = object : JsonObjectRequest(
            Request.Method.POST, url, JSONObject(payload),
            { response ->
                Log.d("Notification", "Powiadomienie wysłane: $response")
            },
            { error ->
                Log.e("Notification", "Błąd wysyłania powiadomienia: ${error.message}")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = mutableMapOf<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "key=b37d19e1b0bcbad045304282e6ebfc0477cf206a"
                return headers
            }
        }
        requestQueue.add(request)
    }

    private fun setupHeader() {
        val profileImageView = findViewById<ImageView>(R.id.chatImageViewUserDoc)
        val userNameTextView = findViewById<TextView>(R.id.chatUserNameDoc)
        val userStatusTextView = findViewById<TextView>(R.id.chatUserStatusDoc)

        FirebaseFirestore.getInstance().collection("users").document(doctorId)
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
