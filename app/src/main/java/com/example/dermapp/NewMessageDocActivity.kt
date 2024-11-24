package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.startDoctor.StartDocActivity

/**
 * Activity for composing and sending new messages from a doctor to a patient.
 */
class NewMessageDocActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView

    /**
     * Initializes the activity layout and sets up UI components.
     * Sets click listener for the back button to navigate to StartDocActivity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_new_message_doc)

        // Set up back button to navigate to StartDocActivity
        val header = findViewById<LinearLayout>(R.id.header_chat)
        backButton = header.findViewById(R.id.chatBackBtn)
        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }
    }
}