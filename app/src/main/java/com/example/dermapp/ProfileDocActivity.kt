package com.example.dermapp

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
import com.example.dermapp.database.Doctor
import com.example.dermapp.database.Patient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileDocActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var buttonEditProfileDoc: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_doc)

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }

        buttonEditProfileDoc = findViewById(R.id.buttonEditProfileDoc)

        buttonEditProfileDoc.setOnClickListener {
            val intent = Intent(this, EditProfileDocActivity::class.java)
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
        val userRef = FirebaseFirestore.getInstance().collection("doctors").document(currentUserUid!!)

        // Pobierz dane użytkownika z Firestore
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Konwertuj dane na obiekt użytkownika
                val user = documentSnapshot.toObject(Doctor::class.java)

                // Sprawdź, czy udało się pobrać dane użytkownika
                user?.let {
                    // Ustaw imię użytkownika
                    val NameTextView: TextView = findViewById(R.id.textViewEnteredFirstNameProfileDoc)
                    NameTextView.text = user.firstName

                    val LastNameTextView: TextView = findViewById(R.id.textViewEnteredLastNameProfileDoc)
                    LastNameTextView.text = user.lastName

                    val EmailTextView: TextView = findViewById(R.id.textViewEnteredEmailProfileDoc)
                    EmailTextView.text = user.email

                    val BirthTextView: TextView = findViewById(R.id.textViewEnteredBirthProfileDoc)
                    BirthTextView.text = user.birthDate

                    val NumberTextView: TextView = findViewById(R.id.textViewEnteredDoctorIdProfileDoc)
                    NumberTextView.text = user.doctorId
                }
            }
        }.addOnFailureListener { exception ->
            // Obsłuż błędy pobierania danych z Firestore
        }
    }
}