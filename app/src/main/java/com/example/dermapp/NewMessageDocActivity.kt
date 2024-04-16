package com.example.dermapp

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NewMessageDocActivity : AppCompatActivity() {

    private lateinit var enterPatientNameEditText: EditText
    private lateinit var enterPatientLastNameEditText: EditText
    private lateinit var enterPatientPeselEditText: EditText
    private lateinit var enterYourMessageEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message_doc)

        // Initialize EditTexts
        enterPatientNameEditText = findViewById(R.id.editTextEnterPatientNameNewMessageDoc)
        enterPatientLastNameEditText = findViewById(R.id.editTextEnterPatientLastNameNewMessageDoc)
        enterPatientPeselEditText = findViewById(R.id.editTextEnterPatientPeselNewMessageDoc)
        enterYourMessageEditText = findViewById(R.id.enterYourMessageNewMessageDoc)

        // Retrieve send ImageView
        val sendImageView = findViewById<ImageView>(R.id.imageSendNewMessageDoc)

        // Set click listener for send ImageView
        sendImageView.setOnClickListener {
            // Perform send message action
            sendMessage()
        }
    }

    private fun sendMessage() {
        // Retrieve input values
        val patientName = enterPatientNameEditText.text.toString().trim()
        val patientLastName = enterPatientLastNameEditText.text.toString().trim()
        val patientPesel = enterPatientPeselEditText.text.toString().trim()
        val message = enterYourMessageEditText.text.toString().trim()

        // Check if all required fields are filled
        if (patientName.isEmpty() || patientLastName.isEmpty() || patientPesel.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Simulate sending the message (replace with your implementation)
        // For demonstration, display a toast message with the entered details
        val sendMessageText = "Sending message to $patientName $patientLastName (PESEL: $patientPesel):\n$message"
        Toast.makeText(this, sendMessageText, Toast.LENGTH_LONG).show()

        // Clear input fields after sending message
        enterPatientNameEditText.text.clear()
        enterPatientLastNameEditText.text.clear()
        enterPatientPeselEditText.text.clear()
        enterYourMessageEditText.text.clear()
    }
}
