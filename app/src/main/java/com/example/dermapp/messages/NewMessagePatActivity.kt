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
import com.example.dermapp.database.Message
import com.example.dermapp.messages.adapter.NewMessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NewMessagePatActivity : AppCompatActivity() {

    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var adapter: NewMessageAdapter
    private val messagesList = mutableListOf<Message>()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var conversationId: String
    private lateinit var receiverId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_new_message_pat)

        // Retrieve data from intent
        receiverId = intent.getStringExtra("receiverId") ?: ""
        conversationId = intent.getStringExtra("conversationId") ?: ""

        // Inicjalizacja widoków
        messagesRecyclerView = findViewById(R.id.messagesRecyclerViewPat)
        messageInput = findViewById(R.id.editTextMessagePat)
        sendButton = findViewById(R.id.sendBtnPat)

        // Pobieranie danych z Intent
        conversationId = intent.getStringExtra("conversationId") ?: ""
        receiverId = intent.getStringExtra("receiverId") ?: ""

        // Obsługa nowej lub istniejącej konwersacji
        if (receiverId.isEmpty()) {
            throw IllegalArgumentException("Receiver ID is missing")
        }

        if (conversationId.isEmpty()) {
            createNewConversation { newConversationId ->
                conversationId = newConversationId
                setupChatHeader(receiverId)
                setupRecyclerView()
                loadMessages()
            }
        } else {
            setupChatHeader(receiverId)
            setupRecyclerView()
            loadMessages()
        }

        // Obsługa przycisku wysyłania wiadomości
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString()
            if (messageText.isNotBlank()) {
                sendMessage(messageText)
                messageInput.text.clear()
            }
        }

        // Initialize the back button
        val backButton: ImageView = findViewById(R.id.chatBackBtn)

        // Set click listener for back button
        backButton.setOnClickListener {
            // Navigate back to MessagesPatActivity
            val intent = Intent(this, MessagesPatActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish() // Close the current activity
        }
    }

    private fun createNewConversation(onComplete: (conversationId: String) -> Unit) {
        val conversationRef = firestore.collection("conversations")
        val newConversationId = conversationRef.document().id

        val newConversation = mapOf(
            "conversationId" to newConversationId,
            "senderId" to currentUserId,
            "receiverId" to receiverId,
            "lastMessage" to "",
            "lastMessageTimestamp" to com.google.firebase.Timestamp.now()
        )

        conversationRef.document(newConversationId)
            .set(newConversation)
            .addOnSuccessListener {
                onComplete(newConversationId)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                throw IllegalStateException("Failed to create a new conversation")
            }
    }

    private fun setupChatHeader(receiverId: String) {
        val profileImageView = findViewById<ImageView>(R.id.chatImageViewUserDoc)
        val nameTextView = findViewById<TextView>(R.id.chatUserNameDoc)
        val statusIndicator = findViewById<TextView>(R.id.chatUserStatusDoc)

        firestore.collection("users").document(receiverId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val name = document.getString("firstName") ?: "Unknown"
                val profilePhotoUrl = document.getString("profilePhoto") ?: ""
                val isOnline = document.getBoolean("isOnline") ?: false

                nameTextView.text = name
                Glide.with(this)
                    .load(profilePhotoUrl)
                    .placeholder(R.drawable.black_account_circle)
                    .circleCrop()
                    .into(profileImageView)

                statusIndicator.text = if (isOnline) "Online" else "Offline"
            } else {
                nameTextView.text = "Unknown User"
                statusIndicator.text = "Offline"
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
        }
    }

    private fun setupRecyclerView() {
        adapter = NewMessageAdapter(this, messagesList)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = adapter
    }

    private fun loadMessages() {
        firestore.collection("messages")
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                    adapter.updateMessages(messages)
                    messagesRecyclerView.scrollToPosition(messages.size - 1)
                } else {
                    messagesList.clear()
                    adapter.updateMessages(messagesList)
                }
            }
    }

    private fun sendMessage(messageText: String) {
        val message = Message(
            messageId = firestore.collection("messages").document().id,
            conversationId = conversationId,
            senderId = currentUserId,
            receiverId = receiverId,
            messageText = messageText,
            timestamp = com.google.firebase.Timestamp.now()
        )

        firestore.collection("messages")
            .document(message.messageId)
            .set(message)
            .addOnSuccessListener {
                updateLastMessage(messageText)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun updateLastMessage(lastMessage: String) {
        firestore.collection("conversations")
            .document(conversationId)
            .update(
                mapOf(
                    "lastMessage" to lastMessage,
                    "lastMessageTimestamp" to com.google.firebase.Timestamp.now()
                )
            )
    }
}
