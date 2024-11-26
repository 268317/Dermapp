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
            firestore.collection("conversation").document(conversationId).update(
                mapOf(
                    "lastMessage" to messageText,
                    "lastMessageTimestamp" to message.timestamp
                )
            ).addOnFailureListener {
                firestore.collection("conversation").document(conversationId).set(
                    mapOf(
                        "conversationId" to conversationId,
                        "doctorId" to currentDoctorId,
                        "patientId" to patientId,
                        "lastMessage" to messageText,
                        "lastMessageTimestamp" to message.timestamp
                    )
                )
            }
        }
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

                // Sprawdzenie, czy aktywność nie została zniszczona
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
