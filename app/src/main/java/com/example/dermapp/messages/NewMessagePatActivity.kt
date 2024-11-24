package com.example.dermapp.messages

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.R

/**
 * Activity for composing and sending new messages from a patient to a doctor.
 */
class NewMessagePatActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView

    /**
     * Initializes the activity layout and sets up UI components.
     * Fetches the doctor's name and surname based on the provided doctorId.
     * Sends the message when the send button is clicked.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_new_message_pat)

        // Set up back button to navigate to MessagesPatActivity
        val header = findViewById<LinearLayout>(R.id.header_chat)
        backButton = header.findViewById(R.id.chatBackBtn)
        backButton.setOnClickListener {
            val intent = Intent(this, MessagesPatActivity::class.java)
            startActivity(intent)
        }
    }
}