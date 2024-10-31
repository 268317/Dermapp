package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.messages.MessagesPatActivity

/**
 * Activity for composing and sending new messages from a patient to a doctor.
 */
class NewMessagePatActivity : AppCompatActivity() {

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
    }
}