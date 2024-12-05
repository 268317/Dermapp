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
 * MessagesActivityDoc is responsible for managing and displaying messages between a doctor
 * and a patient. It includes sending text and photo messages, as well as managing conversations.
 */
class MessagesActivityDoc : AppCompatActivity() {

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
    private var patientId: String? = null

    private val REQUEST_GALLERY_PHOTO = 1
    private val REQUEST_CAMERA_PHOTO = 2

    /**
     * Called when the activity is first created.
     * Initializes the UI and loads messages for the current conversation.
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
            val intent = Intent(this, ChatsActivityDoc::class.java)
            startActivity(intent)
        }

        conversationId = intent.getStringExtra("conversationId")
        patientId = intent.getStringExtra("friendId")

        recyclerView = findViewById(R.id.messagesRecyclerViewPat)
        messageInput = findViewById(R.id.editTextMessagePat)
        sendButton = findViewById(R.id.sendBtnPat)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val headerName: TextView = findViewById(R.id.chatUserNameDoc)
        val headerStatus: TextView = findViewById(R.id.chatUserStatusDoc)
        val headerProfileImage: ImageView = findViewById(R.id.chatImageViewUserDoc)

        // Retrieve data passed from RecentChatsAdapter
        val name = intent.getStringExtra("friendName")
        val profilePhoto = intent.getStringExtra("friendProfilePhoto")

        // Set up UI
        messageAdapter = MessagesAdapter(this, messageList, profilePhoto)
        recyclerView.adapter = messageAdapter

        headerName.text = name ?: "Unknown"
        headerStatus.text = "" // Can be adjusted if a status is needed
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
     * Fetches messages for the current conversation from Firestore and updates the UI.
     */
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

    /**
     * Checks if a conversation exists before sending a message. If not, creates a new one.
     */
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

    /**
     * Saves a message and updates the conversation with the last message details.
     */
    private fun saveMessageAndUpdateConversation(messageText: String) {
        val messageId = firestore.collection("messages").document().id
        val message = hashMapOf(
            "messageId" to messageId,
            "conversationId" to conversationId,
            "senderId" to currentUserId,
            "receiverId" to patientId,
            "messageText" to messageText,
            "timestamp" to FieldValue.serverTimestamp(),
            "isRead" to false
        )

        firestore.collection("messages")
            .document(messageId)
            .set(message)
            .addOnSuccessListener {
                messageInput.text.clear()
                firestore.collection("conversation")
                    .document(conversationId!!)
                    .update(
                        mapOf(
                            "lastMessageId" to messageId
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

    /**
     * Creates a new conversation and saves the first message.
     */
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

    /**
     * Sends a notification to the receiver about a new message.
     *
     * @param messageText The text of the message to be included in the notification.
     */
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

    /**
     * Marks messages in the current conversation as read.
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
     * Shows a dialog for selecting a photo source (gallery or camera).
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
     * Handles the result of gallery or camera actions.
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
     * Sends a photo message to the current conversation.
     *
     * @param photoUrl The URL of the uploaded photo.
     */
    private fun sendPhotoMessage(photoUrl: String) {
        val message = hashMapOf(
            "messageId" to firestore.collection("messages").document().id,
            "conversationId" to conversationId,
            "senderId" to currentUserId,
            "receiverId" to patientId,
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
