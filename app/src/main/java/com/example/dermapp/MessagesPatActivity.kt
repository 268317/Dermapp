package com.example.dermapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MessagesPatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages_pat)

        // Retrieve views
        val imageViewAddMessagesPat = findViewById<ImageView>(R.id.imageViewAddMessagesPat)

        // Set click listener for adding messages
        imageViewAddMessagesPat.setOnClickListener {
            // Implement your logic for adding new messages
            showToast("Add new message clicked")
        }

        // Set click listeners for doctor entries
        setDoctorEntryClickListener(R.id.imageViewDoc1MessagesPat, R.id.textViewDoc1MessagesPat, "Doctor 1")
        setDoctorEntryClickListener(R.id.imageViewDoc2MessagesPat, R.id.textViewDoc2MessagesPat, "Doctor 2")
        setDoctorEntryClickListener(R.id.imageViewDoc3MessagesPat, R.id.textViewDoc3MessagesPat, "Doctor 3")
        setDoctorEntryClickListener(R.id.imageViewDoc4MessagesPat, R.id.textViewDoc4MessagesPat, "Doctor 4")
        setDoctorEntryClickListener(R.id.imageViewDoc5MessagesPat, R.id.textViewDoc5MessagesPat, "Doctor 5")
    }

    private fun setDoctorEntryClickListener(imageViewId: Int, textViewId: Int, doctorName: String) {
        val imageView = findViewById<ImageView>(imageViewId)
        val textView = findViewById<TextView>(textViewId)

        imageView.setOnClickListener {
            // Handle click on doctor image
            showToast("Clicked on $doctorName's image")
        }

        textView.setOnClickListener {
            // Handle click on doctor name
            showToast("Clicked on $doctorName's name")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
