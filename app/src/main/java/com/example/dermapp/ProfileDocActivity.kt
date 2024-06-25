package com.example.dermapp

import com.example.dermapp.database.Doctor
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity to display and edit doctor profile information.
 */
class ProfileDocActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var buttonEditProfileDoc: Button

    /**
     * Initializes the activity layout and sets up UI components.
     * Also retrieves and displays doctor profile information from Firestore.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display

        // Set the activity layout
        setContentView(R.layout.activity_profile_doc)

        // Initialize views
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        // Navigate back to StartDocActivity when back button is clicked
        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }

        // Setup edit profile button to navigate to EditProfileDocActivity
        buttonEditProfileDoc = findViewById(R.id.buttonEditProfileDoc)
        buttonEditProfileDoc.setOnClickListener {
            val intent = Intent(this, EditProfileDocActivity::class.java)
            startActivity(intent)
        }

        // Apply window insets to handle system UI elements
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get UID of the currently logged-in user
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Reference to the user document in Firestore
        val userRef = FirebaseFirestore.getInstance().collection("doctors").document(currentUserUid!!)

        // Retrieve user data from Firestore
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Convert data to Doctor object
                val user = documentSnapshot.toObject(Doctor::class.java)

                // Check if user data retrieval was successful
                user?.let {
                    // Set user's first name
                    val NameTextView: TextView = findViewById(R.id.textViewEnteredFirstNameProfileDoc)
                    NameTextView.text = user.firstName

                    // Set user's last name
                    val LastNameTextView: TextView = findViewById(R.id.textViewEnteredLastNameProfileDoc)
                    LastNameTextView.text = user.lastName

                    // Set user's email address
                    val EmailTextView: TextView = findViewById(R.id.textViewEnteredEmailProfileDoc)
                    EmailTextView.text = user.email

                    // Set user's date of birth
                    val BirthTextView: TextView = findViewById(R.id.textViewEnteredBirthProfileDoc)
                    BirthTextView.text = user.birthDate

                    // Set user's doctor ID
                    val NumberTextView: TextView = findViewById(R.id.textViewEnteredDoctorIdProfileDoc)
                    NumberTextView.text = user.doctorId
                }
            }
        }.addOnFailureListener { exception ->
            // Handle errors in Firestore data retrieval
            // You might want to log or display an error message
        }
    }
}