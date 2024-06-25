package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.messages.MessagesPatActivity
import com.example.dermapp.database.Doctor
import com.example.dermapp.database.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

/**
 * Activity for composing and sending new messages from a patient to a doctor.
 */
class NewMessagePatActivity : AppCompatActivity() {

    private lateinit var enterYourMessageEditText: EditText
    private lateinit var doctorNameSurnameEditText: EditText
    private lateinit var backButton: ImageButton

    /**
     * Initializes the activity layout and sets up UI components.
     * Fetches the doctor's name and surname based on the provided doctorId.
     * Sends the message when the send button is clicked.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message_pat)

        // Set up back button to navigate to MessagesPatActivity
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MessagesPatActivity::class.java)
            startActivity(intent)
        }

        // Initialize views
        enterYourMessageEditText = findViewById(R.id.enterYourMessageNewMessagePat)
        doctorNameSurnameEditText = findViewById(R.id.autoCompleteTextViewDoctor)

        // Disable editing of doctor name and surname field
        doctorNameSurnameEditText.isEnabled = false

        // Retrieve doctorId from intent and fetch doctor's name and surname
        val doctorId = intent.getStringExtra("doctorId")
        doctorId?.let {
            fetchDoctorNameAndSurname(it)
        }

        // Set up send button to send the message
        val sendImageView = findViewById<ImageView>(R.id.imageSendNewMessagePat)
        sendImageView.setOnClickListener {
            sendMessage()
        }
    }

    /**
     * Fetches the doctor's name and surname from Firestore based on the provided doctorId.
     * Updates the UI to display the fetched doctor's name and surname.
     */
    private fun fetchDoctorNameAndSurname(doctorId: String) {
        FirebaseFirestore.getInstance().collection("doctors").document(doctorId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val doctor = documentSnapshot.toObject(Doctor::class.java)
                    doctor?.let {
                        val doctorNameSurname = "${it.firstName} ${it.lastName}"
                        doctorNameSurnameEditText.setText(doctorNameSurname)
                    }
                } else {
                    Toast.makeText(this, "Doctor not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch doctor's details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Sends the message entered by the patient to the doctor.
     * Validates the message text and handles success and failure cases of sending the message to Firestore.
     */
    private fun sendMessage() {
        // Retrieve doctorId from intent
        val doctorId = intent.getStringExtra("doctorId") ?: return

        // Get the message text entered by the patient
        val message = enterYourMessageEditText.text.toString().trim()

        // Validate if the message is empty
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter your message", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current user's UID (patient's UID)
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Example patient PESEL (replace with actual patient identifier logic if needed)
        val patientPesel = "12345678901"

        // Create a new message object
        val messageObject = Message(
            doctorId = doctorId,
            patientId = currentUserUid,
            messageText = message
        )

        // Add the message to Firestore messages collection
        FirebaseFirestore.getInstance().collection("messages").add(messageObject)
            .addOnSuccessListener { documentReference ->
                // Set the messageId to be the same as the Firestore document ID
                val messageId = documentReference.id
                messageObject.messageId = messageId

                // Update the document with the new messageId
                FirebaseFirestore.getInstance().collection("messages").document(messageId).set(messageObject)
                    .addOnSuccessListener {
                        // Display success message and clear message input field
                        Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show()
                        enterYourMessageEditText.text.clear()
                    }
                    .addOnFailureListener { e ->
                        // Handle failure to update messageId
                        Toast.makeText(this, "Failed to update messageId: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                // Handle failure to send message to Firestore
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}