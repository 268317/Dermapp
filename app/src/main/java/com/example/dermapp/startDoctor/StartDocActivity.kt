package com.example.dermapp.startDoctor

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.CreateNewReportActivity
import com.example.dermapp.MainActivity
import com.example.dermapp.MakeAppointmentDocActivity
import com.example.dermapp.ProfileDocActivity
import com.example.dermapp.R
import com.example.dermapp.database.AppUser
import com.example.dermapp.database.Appointment
import com.example.dermapp.database.MedicalReport
import com.example.dermapp.database.Prescription
import com.example.dermapp.messages.MessagesDocActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StartDocActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: ImageButton
    private lateinit var navView: NavigationView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_doc)

        drawerLayout = findViewById(R.id.drawer_layout)

        val recyclerViewAppointments = findViewById<RecyclerView>(R.id.RVstartDocAppointment)
        recyclerViewAppointments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val appointments: MutableList<Appointment> = ArrayList()

        val recyclerViewReports = findViewById<RecyclerView>(R.id.RVstartDocReport)
        recyclerViewReports.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val reports: MutableList<MedicalReport> = ArrayList()

        val recyclerViewPrescriptions = findViewById<RecyclerView>(R.id.RVstartDocPrescription)
        recyclerViewPrescriptions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val prescriptions: MutableList<Prescription> = ArrayList()

//        appointments.add(
//            Appointment(
//                doctorId = "Jan",
//                patientPesel = "Kowalski",
//                appointmentDate = Date()
//            )
//        )
//
//        appointments.add(
//            Appointment(
//                doctorId = "Adam",
//                patientPesel = "Nowak",
//                appointmentDate = Date()
//            )
//        )
//
//        appointments.add(
//            Appointment(
//                doctorId = "Monika",
//                patientPesel = "Adamska",
//                appointmentDate = Date()
//            )
//        )
//
//        appointments.add(
//            Appointment(
//                doctorId = "Anna",
//                patientPesel = "Kwiatek",
//                appointmentDate = Date()
//            )
//        )

        reports.add(
            MedicalReport(
                doctorId = "Jan",
                patientPesel = "Kowalski",
                date = "10-06-2024"
            )
        )

        reports.add(
            MedicalReport(
                doctorId = "Jan",
                patientPesel = "Kowalski",
                date = "10-06-2024"
            )
        )

        reports.add(
            MedicalReport(
                doctorId = "Jan",
                patientPesel = "Kowalski",
                date = "10-06-2024"
            )
        )

//        prescriptions.add(
//            Prescription(
//                doctorId = "Jan",
//                patientId = "Kowalski",
//                date = "10-06-2024".
//            )
//        )
//
//        prescriptions.add(
//            Prescription(
//                doctorId = "Jan",
//                patientId = "Kowalski",
//                date = "10-06-2024"
//            )
//        )
//
//        prescriptions.add(
//            Prescription(
//                doctorId = "Jan",
//                patientId = "Kowalski",
//                date = "10-06-2024"
//            )
//        )

        // Set layout manager and adapter for RecyclerView
        recyclerViewAppointments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewAppointments.adapter = MyAdapterStartDocAppointment(appointments)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RVstartDocAppointment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set layout manager and adapter for RecyclerView
        recyclerViewReports.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewReports.adapter = MyAdapterStartDocReport(reports)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RVstartDocReport)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set layout manager and adapter for RecyclerView
        recyclerViewPrescriptions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewPrescriptions.adapter = MyAdapterStartDocPrescription(prescriptions)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RVstartDocPrescription)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val header = findViewById<RelativeLayout>(R.id.includeHeaderDoc)
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
                R.id.nav_newReport -> {
                    val intent = Intent(this, CreateNewReportActivity::class.java)
                    startActivity(intent)
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
        val userRef = FirebaseFirestore.getInstance().collection("doctors").document(currentUserUid!!)

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
