package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.dermapp.database.Patient
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity to display and edit patient profile information.
 */
class ProfilePatActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var buttonEditProfilePat: Button
    private lateinit var profileImage: ImageView

    /**
     * Initializes the activity layout and sets up UI components.
     * Also retrieves and displays patient profile information from Firestore.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display

        // Set the activity layout
        setContentView(R.layout.activity_profile_pat)

        // Initialize views
        profileImage = findViewById(R.id.profileImagePat)
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        // Navigate back to StartPatActivity when back button is clicked
        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        // Setup edit profile button to navigate to EditProfilePatActivity
        buttonEditProfilePat = findViewById(R.id.buttonEditProfilePat)
        buttonEditProfilePat.setOnClickListener {
            val intent = Intent(this, EditProfilePatActivity::class.java)
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
        val userRef = FirebaseFirestore.getInstance().collection("patients").document(currentUserUid!!)

        // Retrieve user data from Firestore
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Convert data to Patient object
                val user = documentSnapshot.toObject(Patient::class.java)

                // Check if user data retrieval was successful
                user?.let {
                    // Set user's first name
                    val NameTextView: TextView = findViewById(R.id.textViewEnteredFirstNameProfilePat)
                    NameTextView.text = user.firstName

                    // Set user's last name
                    val LastNameTextView: TextView = findViewById(R.id.textViewEnteredLastNameProfilePat)
                    LastNameTextView.text = user.lastName

                    // Set user's email address
                    val EmailTextView: TextView = findViewById(R.id.textViewEnteredEmailProfilePat)
                    EmailTextView.text = user.email

                    // Set user's date of birth
                    val BirthTextView: TextView = findViewById(R.id.textViewEnteredBirthProfilePat)
                    BirthTextView.text = user.birthDate

                    // Set user's PESEL (Personal Identification Number)
                    val PeselTextView: TextView = findViewById(R.id.textViewEnteredPeselProfilePat)
                    PeselTextView.text = user.pesel

                    // Check if profile photo URL exists and set it using Glide
                    val profilePhotoUrl = user.profilePhoto
                    if (!profilePhotoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profilePhotoUrl)
                            .placeholder(R.drawable.black_account_circle) // Optional placeholder
                            .into(profileImage)
                    } else {
                        // Optionally set a default avatar if no URL is available
                        profileImage.setImageResource(R.drawable.black_account_circle)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            // Handle errors in Firestore data retrieval
            // You might want to log or display an error message
        }
    }
}
