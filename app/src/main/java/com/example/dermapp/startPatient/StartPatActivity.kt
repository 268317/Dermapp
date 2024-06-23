package com.example.dermapp.startPatient

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.CreateNewReportActivity
import com.example.dermapp.MainActivity
import com.example.dermapp.MakeAppointmentPatActivity
import com.example.dermapp.ProfilePatActivity
import com.example.dermapp.R
import com.example.dermapp.database.AppUser
import com.example.dermapp.database.Appointment
import com.example.dermapp.database.MedicalReport
import com.example.dermapp.database.Prescription
import com.example.dermapp.messages.MessagesPatActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StartPatActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: ImageButton
    private lateinit var navView: NavigationView

    private lateinit var recyclerViewAppointments: RecyclerView
    private lateinit var recyclerViewReports: RecyclerView
    private lateinit var recyclerViewPrescriptions: RecyclerView

    private lateinit var appointmentsAdapter: MyAdapterStartPatAppointment
    private lateinit var reportsAdapter: MyAdapterStartPatReport
    private lateinit var prescriptionsAdapter: MyAdapterStartPatPrescription

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_patient)

        drawerLayout = findViewById(R.id.drawer_layout)

        recyclerViewAppointments = findViewById(R.id.RVstartPatAppointment)
        recyclerViewAppointments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        appointmentsAdapter = MyAdapterStartPatAppointment(mutableListOf(), this)
        recyclerViewAppointments.adapter = appointmentsAdapter

        recyclerViewReports = findViewById(R.id.RVstartPatReport)
        recyclerViewReports.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        reportsAdapter = MyAdapterStartPatReport(mutableListOf(), this)
        recyclerViewReports.adapter = reportsAdapter

        recyclerViewPrescriptions = findViewById(R.id.RVstartPatPrescription)
        recyclerViewPrescriptions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        prescriptionsAdapter = MyAdapterStartPatPrescription(mutableListOf(), this)
        recyclerViewPrescriptions.adapter = prescriptionsAdapter

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
                    val intent = Intent(this, ProfilePatActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_make -> {
                    val intent = Intent(this, MakeAppointmentPatActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_newReport -> {
                    val intent = Intent(this, CreateNewReportActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_myMailbox -> {
                    val intent = Intent(this, MessagesPatActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        fetchAppointments()
        fetchReports()
        fetchPrescriptions()


        // Pobierz UID aktualnie zalogowanego użytkownika
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Utwórz odwołanie do dokumentu użytkownika w Firestore
        val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserUid!!)

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
        }


    }

    private fun fetchAppointments() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val appointmentsCollection = FirebaseFirestore.getInstance().collection("appointment")

        currentUserUid?.let { uid ->
            appointmentsCollection
                .whereEqualTo("patientId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    val appointments = mutableListOf<Appointment>()
                    for (document in documents) {
                        val appointment = document.toObject(Appointment::class.java)
                        appointments.add(appointment)
                    }
                    appointmentsAdapter.updateAppointments(appointments)
                }
                .addOnFailureListener { exception ->
                    // Handle errors
                    // For example, Log.e(TAG, "Error fetching appointments", exception)
                }
        }
    }

    private fun fetchReports() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        currentUserUid?.let { uid ->
            val userPatRef = FirebaseFirestore.getInstance().collection("patients").document(uid)
            Log.d(TAG, "User uid: ${currentUserUid}")

            userPatRef.get().addOnSuccessListener { document ->
                val currentUserPesel = document.getString("pesel")
                Log.d(TAG, "User pesel: ${currentUserPesel}")
                currentUserPesel?.let { pesel ->
                    val reportsCollection = FirebaseFirestore.getInstance().collection("report")
                    reportsCollection
                        .whereEqualTo("patientPesel", pesel)
                        .get()
                        .addOnSuccessListener { documents ->
                            val reports = mutableListOf<MedicalReport>()
                            for (document in documents) {
                                val report = document.toObject(MedicalReport::class.java)
                                reports.add(report)
                                Log.d(TAG, "Report: ${report}")
                            }
                            reportsAdapter.updateReports(reports)
                        }
                        .addOnFailureListener { exception ->
                        }
                } ?: run {
                }
            }.addOnFailureListener { exception ->
            }
        }
    }

    private fun fetchPrescriptions() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val prescriptionsCollection = FirebaseFirestore.getInstance().collection("prescription")

        currentUserUid?.let { uid ->
            prescriptionsCollection
                .whereEqualTo("patientId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    val prescriptions = mutableListOf<Prescription>()
                    for (document in documents) {
                        val prescription = document.toObject(Prescription::class.java)
                        prescriptions.add(prescription)
                    }
                    prescriptionsAdapter.updatePrescriptions(prescriptions)
                }
                .addOnFailureListener { exception ->
                    // Handle errors
                    // For example, Log.e(TAG, "Error fetching prescriptions", exception)
                }
        }
    }
}
