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

class NewMessagePatActivity : AppCompatActivity() {

    private lateinit var enterYourMessageEditText: EditText
    private lateinit var doctorNameSurnameEditText: EditText
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message_pat)

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, MessagesPatActivity::class.java)
            startActivity(intent)
        }

        enterYourMessageEditText = findViewById(R.id.enterYourMessageNewMessagePat)
        doctorNameSurnameEditText = findViewById(R.id.autoCompleteTextViewDoctor)

        doctorNameSurnameEditText.isEnabled = false

        val doctorId = intent.getStringExtra("doctorId")
        doctorId?.let {
            fetchDoctorNameAndSurname(it)
        }

        val sendImageView = findViewById<ImageView>(R.id.imageSendNewMessagePat)

        sendImageView.setOnClickListener {
            sendMessage()
        }
    }

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

    private fun sendMessage() {
        val doctorId = intent.getStringExtra("doctorId") ?: return
        val message = enterYourMessageEditText.text.toString().trim()

        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter your message", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val patientPesel = "12345678901" // Replace with actual patient PESEL retrieval logic

        // Create a new message object
        val messageObject = Message(
            doctorId = doctorId,
            patientId = currentUserUid,
            messageText = message
        )

        // Add the message to Firestore
        FirebaseFirestore.getInstance().collection("messages").add(messageObject)
            .addOnSuccessListener { documentReference ->
                // Set the messageId to be the same as the Firestore document ID
                val messageId = documentReference.id
                messageObject.messageId = messageId

                // Update the document with the new messageId
                FirebaseFirestore.getInstance().collection("messages").document(messageId).set(messageObject)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show()
                        enterYourMessageEditText.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update messageId: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
