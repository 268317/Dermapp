package com.example.dermapp

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NewMessagePatActivity : AppCompatActivity() {

    private lateinit var enterDoctorNameEditText: EditText
    private lateinit var enterDoctorLastNameEditText: EditText
    private lateinit var enterDoctorIdEditText: EditText
    private lateinit var enterYourMessageEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message_pat)

        // Initialize EditTexts
        enterDoctorNameEditText = findViewById(R.id.editTextEnterDoctorNameNewMessagePat)
        enterDoctorLastNameEditText = findViewById(R.id.editTexEnterDoctorLastNameNewMessagePat)
        enterDoctorIdEditText = findViewById(R.id.editTextEnterDoctorIdNewMessagePat)
        enterYourMessageEditText = findViewById(R.id.enterYourMessageNewMessagePat)

        // Retrieve send ImageView
        val sendImageView = findViewById<ImageView>(R.id.imageSendNewMessagePat)

        // Set click listener for send ImageView
        sendImageView.setOnClickListener {
            // Perform send message action
            sendMessage()
        }
    }

    private fun sendMessage() {
        // Retrieve input values
        val doctorName = enterDoctorNameEditText.text.toString().trim()
        val doctorLastName = enterDoctorLastNameEditText.text.toString().trim()
        val doctorId = enterDoctorIdEditText.text.toString().trim()
        val message = enterYourMessageEditText.text.toString().trim()

        // Check if all required fields are filled
        if (doctorName.isEmpty() || doctorLastName.isEmpty() || doctorId.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Simulate sending the message (replace with your implementation)
        // For demonstration, display a toast message with the entered details
        val sendMessageText = "Sending message to Dr. $doctorName $doctorLastName (ID: $doctorId):\n$message"
        Toast.makeText(this, sendMessageText, Toast.LENGTH_LONG).show()

        // Clear input fields after sending message
        enterDoctorNameEditText.text.clear()
        enterDoctorLastNameEditText.text.clear()
        enterDoctorIdEditText.text.clear()
        enterYourMessageEditText.text.clear()
    }
}
