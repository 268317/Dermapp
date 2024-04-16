package com.example.dermapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MessagesDocActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages_doc)

        // Retrieve views
        val imageViewAddMessagesDoc = findViewById<ImageView>(R.id.imageViewAddMessagesDoc)

        // Set click listener for adding messages
        imageViewAddMessagesDoc.setOnClickListener {
            // Implement your logic for adding new messages
            Toast.makeText(this, "Add new message clicked", Toast.LENGTH_SHORT).show()
        }

        // Set click listeners for patient entries
        setPatientEntryClickListener(R.id.imageViewPat1MessagesDoc, R.id.textViewPat1MessagesDoc, "Patient 1")
        setPatientEntryClickListener(R.id.imageViewPat2MessagesDoc, R.id.textViewPat2MessagesDoc, "Patient 2")
        setPatientEntryClickListener(R.id.imageViewPat3MessagesDoc, R.id.textViewPat3MessagesDoc, "Patient 3")
        setPatientEntryClickListener(R.id.imageViewPat4MessagesDoc, R.id.textViewPat4MessagesDoc, "Patient 4")
        setPatientEntryClickListener(R.id.imageViewPat5MessagesDoc, R.id.textViewPat5MessagesDoc, "Patient 5")
    }

    private fun setPatientEntryClickListener(imageViewId: Int, textViewId: Int, patientName: String) {
        val imageView = findViewById<ImageView>(imageViewId)
        val textView = findViewById<TextView>(textViewId)

        imageView.setOnClickListener {
            // Handle click on patient image
            showToast("Clicked on $patientName's image")
        }

        textView.setOnClickListener {
            // Handle click on patient name
            showToast("Clicked on $patientName's name")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
