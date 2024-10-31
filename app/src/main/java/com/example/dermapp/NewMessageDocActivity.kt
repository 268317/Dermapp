package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.startDoctor.StartDocActivity

/**
 * Activity for composing and sending new messages from a doctor to a patient.
 */
class NewMessageDocActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton

    /**
     * Initializes the activity layout and sets up UI components.
     * Sets click listener for the back button to navigate to StartDocActivity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message_doc)

        // Set up back button to navigate to StartDocActivity
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }
    }
}