package com.example.dermapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.startDoctor.StartDocActivity
import java.util.TimeZone

/**
 * Activity for displaying the doctor's main menu.
 * Enables edge-to-edge display and handles navigation back to StartDocActivity.
 */
class MenuDocActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton

    /**
     * Called when the activity is starting.
     * Sets up the UI components and listeners, including enabling edge-to-edge display.
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display

        // Ustawienie strefy czasowej dla aktywności
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        // Set up back button click listener
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }
    }
}