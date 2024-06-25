package com.example.dermapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.startPatient.StartPatActivity
import com.google.android.material.navigation.NavigationView

/**
 * Activity for displaying the patient's main menu.
 * This activity provides navigation options for the patient.
 */
class MenuPatActivity : AppCompatActivity() {

    private lateinit var navView: NavigationView
    private lateinit var backButton: ImageButton

    /**
     * Called when the activity is starting.
     * Sets up the UI components and listeners, including back button click listener.
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up back button click listener
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }
    }
}
