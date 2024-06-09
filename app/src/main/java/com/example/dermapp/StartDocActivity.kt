package com.example.dermapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.dermapp.database.AppUser
import com.example.dermapp.messages.MessagesDocActivity
import com.example.dermapp.messages.MessagesPatActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StartDocActivity : AppCompatActivity(){
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: ImageButton
    private lateinit var navView: NavigationView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_doc)

        drawerLayout = findViewById(R.id.drawer_layout)

        val header = findViewById<RelativeLayout>(R.id.includeHeader)
        menuButton = header.findViewById(R.id.menuButton)

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }


        navView = findViewById(R.id.nav_view)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_myProfile -> {
                    val intent = Intent(this, ProfileDocActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_make -> {
                    val intent = Intent(this, MakeAppointmentDocActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_myAppointments -> {
                    Toast.makeText(this, "My appointments clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_writePrescription -> {
                    val intent = Intent(this, "Write prescription clicked"::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_myReports -> {
                    Toast.makeText(this, "Reports clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_myPrescriptions -> {
                    Toast.makeText(this, "Prescription history clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_myMailbox -> {
                    val intent = Intent(this, MessagesDocActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Pobierz UID aktualnie zalogowanego użytkownika
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Utwórz odwołanie do dokumentu użytkownika w Firestore
        val userRef = FirebaseFirestore.getInstance().collection("patients").document(currentUserUid!!)

        // Pobierz dane użytkownika z Firestore
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Konwertuj dane na obiekt użytkownika
                val user = documentSnapshot.toObject(AppUser::class.java)

                // Sprawdź, czy udało się pobrać dane użytkownika
                user?.let {
                    // Ustaw imię użytkownika w nagłówku
                    val headerNameTextView: TextView = findViewById(R.id.firstNameTextView)
                    headerNameTextView.text = user.firstName
                }
            }
        }.addOnFailureListener { exception ->
            // Obsłuż błędy pobierania danych z Firestore
        }
    }

}