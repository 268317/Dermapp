package com.example.dermapp.chat.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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
import com.google.firebase.storage.FirebaseStorage
import java.io.File

/**
 * MessagesActivityPat is responsible for handling messages between a patient and a doctor.
 * It manages text and photo messages, along with conversation updates.
 */
class MessagesActivityPat : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: ImageView

    private val messageList = mutableListOf<Message>()

    // Instance of Firebase Firestore for database operations
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // Instance of Firebase Functions for sending notifications
    private val functions by lazy { FirebaseFunctions.getInstance() }

    // Current logged-in user's ID
    private val currentUserId by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    private var conversationId: String? = null
    private var doctorId: String? = null

    private val REQUEST_GALLERY_PHOTO = 1
    private val REQUEST_CAMERA_PHOTO = 2

    /**
     * Called when the activity is first created.
     * Sets up UI and initializes the chat functionality.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_new_message)

        val attachPhotoBtn: Button = findViewById(R.id.attachPhotoBtn)
        attachPhotoBtn.setOnClickListener {
            showPhotoSourceDialog()
        }

        val backHeader = findViewById<LinearLayout>(R.id.header_chat)
        backButton = backHeader.findViewById(R.id.chatBackBtn)
        backButton.setOnClickListener {
            val intent = Intent(this, ChatsActivityPat::class.java)
            startActivity(intent)
        }

        conversationId = intent.getStringExtra("conversationId")
        doctorId = intent.getStringExtra("friendId")
        Log.d("MessagesActivityPat", "Received conversationId: $conversationId")
        Log.d("MessagesActivityPat", "Received doctorId: $doctorId")

        recyclerView = findViewById(R.id.messagesRecyclerViewPat)
        messageInput = findViewById(R.id.editTextMessagePat)
        sendButton = findViewById(R.id.sendBtnPat)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val headerName: TextView = findViewById(R.id.chatUserNameDoc)
        val headerProfileImage: ImageView = findViewById(R.id.chatImageViewUserDoc)

        // Retrieve data passed from RecentChatsAdapter
        val name = intent.getStringExtra("friendName")
        val profilePhoto = intent.getStringExtra("friendProfilePhoto")
        Log.d("MessagesActivityPat", "Received friendName: $name")
        Log.d("MessagesActivityPat", "Received friendProfilePhoto: $profilePhoto")

        // Set up UI
        messageAdapter = MessagesAdapter(this, messageList, profilePhoto)
        recyclerView.adapter = messageAdapter

        headerName.text = name ?: "Unknown"
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

    /**
     * Fetches messages from Firestore for the current conversation and updates the RecyclerView.
     */
    private fun fetchMessages() {
        if (conversationId == null) {
            Log.e("MessagesActivityPat", "Conversation ID is null, cannot fetch messages")
            return
        }

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
                    Log.d("MessagesActivityPat", "Fetched ${messageList.size} messages")
                    messageAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messageList.size - 1)

                    markMessagesAsRead()
                }
            }
    }

    /**
     * Checks if the conversation exists and sends a message.
     * If the conversation does not exist, it creates a new one.
     */
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

    /**
     * Saves a message and updates the conversation's last message details in Firestore.
     */
    private fun saveMessageAndUpdateConversation(messageText: String) {
        Log.d("MessagesActivityPat", "Saving message to conversationId: $conversationId")
        val messageId = firestore.collection("messages").document().id
        val message = hashMapOf(
            "messageId" to messageId,
            "conversationId" to conversationId,
            "senderId" to currentUserId,
            "receiverId" to doctorId,
            "messageText" to messageText,
            "timestamp" to FieldValue.serverTimestamp(),
            "isRead" to false
        )

        firestore.collection("messages")
            .document(messageId)
            .set(message)
            .addOnSuccessListener {
                Log.d("MessagesActivityPat", "Message sent successfully")
                messageInput.text.clear()
                firestore.collection("conversation")
                    .document(conversationId!!)
                    .update(
                        mapOf(
                            "lastMessageId" to messageId
                        )
                    )
                    .addOnFailureListener { e ->
                        Log.e("MessagesActivityPat", "Error updating conversation", e)
                    }

                sendNotificationToReceiver(messageText)
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivityPat", "Error sending message", e)
            }
    }

    /**
     * Creates a new conversation in Firestore and saves the first message.
     */
    private fun createConversationAndSaveMessage(messageText: String) {
        Log.d("MessagesActivityPat", "Creating new conversation for conversationId: $conversationId")
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

    /**
     * Sends a notification to the doctor about a new message.
     *
     * @param messageText The text of the message to be included in the notification.
     */
    private fun sendNotificationToReceiver(messageText: String) {
        firestore.collection("users")
            .document(doctorId!!)
            .get()
            .addOnSuccessListener { document ->
                val deviceToken = document.getString("deviceToken")
                if (deviceToken != null) {
                    val notificationData = hashMapOf(
                        "to" to deviceToken,
                        "notification" to hashMapOf(
                            "title" to "New Message",
                            "body" to messageText
                        )
                    )

                    functions.getHttpsCallable("sendNotification")
                        .call(notificationData)
                        .addOnSuccessListener {
                            Log.d("MessagesActivityPat", "Notification sent successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("MessagesActivityPat", "Failed to send notification", e)
                        }
                } else {
                    Log.e("MessagesActivityPat", "Device token not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivityPat", "Failed to fetch user data", e)
            }
    }

    /**
     * Marks unread messages in the conversation as read in Firestore.
     */
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

    /**
     * Displays a dialog for choosing the source of a photo (gallery or camera).
     */
    private fun showPhotoSourceDialog() {
        val options = arrayOf("Choose from Gallery", "Take a Photo")
        AlertDialog.Builder(this)
            .setTitle("Attach Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .show()
    }

    /**
     * Opens the gallery for selecting a photo.
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_GALLERY_PHOTO)
    }

    private lateinit var cameraPhotoUri: Uri

    /**
     * Opens the camera for taking a new photo.
     */
    private fun openCamera() {
        val photoFile = File.createTempFile("IMG_", ".jpg", externalCacheDir).apply {
            createNewFile()
        }
        cameraPhotoUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri)
        }
        startActivityForResult(intent, REQUEST_CAMERA_PHOTO)
    }

    /**
     * Handles the result of photo selection from gallery or camera.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLERY_PHOTO -> {
                    val selectedImageUri = data?.data
                    selectedImageUri?.let { uploadPhotoAndSendMessage(it) }
                }
                REQUEST_CAMERA_PHOTO -> {
                    uploadPhotoAndSendMessage(cameraPhotoUri)
                }
            }
        }
    }

    /**
     * Uploads a photo to Firebase Storage and sends it as a message.
     *
     * @param photoUri The URI of the selected photo.
     */
    private fun uploadPhotoAndSendMessage(photoUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("message_photos/${System.currentTimeMillis()}.jpg")

        storageRef.putFile(photoUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    sendPhotoMessage(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivity", "Failed to upload photo", e)
            }
    }

    /**
     * Sends a photo message in the current conversation.
     *
     * @param photoUrl The URL of the uploaded photo.
     */
    private fun sendPhotoMessage(photoUrl: String) {
        val message = hashMapOf(
            "messageId" to firestore.collection("messages").document().id,
            "conversationId" to conversationId,
            "senderId" to currentUserId,
            "receiverId" to doctorId,
            "photoUrl" to photoUrl,
            "timestamp" to FieldValue.serverTimestamp(),
            "isRead" to false
        )

        firestore.collection("messages")
            .add(message)
            .addOnSuccessListener {
                Log.d("MessagesActivity", "Photo message sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("MessagesActivity", "Failed to send photo message", e)
            }
    }
}
