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
import com.example.dermapp.database.Patient
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URL

class ProfilePatActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var buttonEditProfilePat: Button
    private lateinit var profileImage: ImageView
    private lateinit var imageUrl: URL
    private lateinit var myUrl: URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_pat)

        profileImage = findViewById<ImageView>(R.id.profileImagePat)


        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        buttonEditProfilePat = findViewById(R.id.buttonEditProfilePat)

        buttonEditProfilePat.setOnClickListener {
            val intent = Intent(this, EditProfilePatActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Pobierz UID aktualnie zalogowanego użytkownika
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Utwórz odwołanie do dokumentu użytkownika w Firestore
        val userRef = FirebaseFirestore.getInstance().collection("patients").document(currentUserUid!!)

        // Pobierz dane użytkownika z Firestore
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Konwertuj dane na obiekt użytkownika
                val user = documentSnapshot.toObject(Patient::class.java)

                // Sprawdź, czy udało się pobrać dane użytkownika
                user?.let {
                    // Ustaw imię użytkownika
                    val NameTextView: TextView = findViewById(R.id.textViewEnteredFirstNameProfilePat)
                    NameTextView.text = user.firstName

                    val LastNameTextView: TextView = findViewById(R.id.textViewEnteredLastNameProfilePat)
                    LastNameTextView.text = user.lastName

                    val EmailTextView: TextView = findViewById(R.id.textViewEnteredEmailProfilePat)
                    EmailTextView.text = user.email

                    val BirthTextView: TextView = findViewById(R.id.textViewEnteredBirthProfilePat)
                    BirthTextView.text = user.birthDate

                    val PeselTextView: TextView = findViewById(R.id.textViewEnteredPeselProfilePat)
                    PeselTextView.text = user.pesel
                }
            }
        }.addOnFailureListener { exception ->
            // Obsłuż błędy pobierania danych z Firestore
        }
    }
}