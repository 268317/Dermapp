package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.database.AppUser
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppointmentDetailsPatActivity : AppCompatActivity() {

    // Declare UI elements
    private lateinit var textViewAppointmentDatePat: TextView
    private lateinit var textViewDateAppointmentPat: TextView
    private lateinit var textViewDoctorAppointmentPat: TextView
    private lateinit var textViewFirstNameAppointmentPat: TextView
    private lateinit var textViewLastNameAppointmentPat: TextView
    private lateinit var textViewDocIDAppointmentPat: TextView
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_details_pat)

        // Initialize UI elements
        textViewAppointmentDatePat = findViewById(R.id.textViewAppointmentDatePat)
        textViewDateAppointmentPat = findViewById(R.id.textViewDateAppointmentPat)
        textViewDoctorAppointmentPat = findViewById(R.id.textViewDoctorAppointmentPat)
        textViewFirstNameAppointmentPat = findViewById(R.id.textViewFirstNameAppointmentPat)
        textViewLastNameAppointmentPat = findViewById(R.id.textViewLastNameAppointmentPat)
        textViewDocIDAppointmentPat = findViewById(R.id.textViewDocIDAppointmentPat)

        // Retrieve passed data
        val appointmentId = intent.getStringExtra("appointmentId")
        val appointmentDate = intent.getStringExtra("appointmentDate")
        val doctorId = intent.getStringExtra("doctorId")

        // Set data to the TextViews
        textViewAppointmentDatePat.text = "Appointment date:"
        textViewDateAppointmentPat.text = appointmentDate ?: "Unknown"
        textViewDoctorAppointmentPat.text = "Doctor ID:"
        textViewDocIDAppointmentPat.text = doctorId ?: "Unknown"

        // Set up back button click listener
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        // Retrieve currently logged in user's UID
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Create reference to user document in Firestore
        currentUserUid?.let { uid ->
            val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)

            // Get user data from Firestore
            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Convert data to AppUser object
                    val user = documentSnapshot.toObject(AppUser::class.java)

                    // Update UI with user's first name
                    user?.let {
                        val headerNameTextView: TextView = findViewById(R.id.firstNameTextView)
                        headerNameTextView.text = user.firstName
                    }
                }
            }.addOnFailureListener { exception ->
                // Handle errors fetching data from Firestore
            }
        }
    }
}
