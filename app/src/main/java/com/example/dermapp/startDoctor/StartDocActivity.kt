package com.example.dermapp.startDoctor

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.AppointmentsDocActivity
import com.example.dermapp.CreateNewReportActivity
import com.example.dermapp.MainActivity
import com.example.dermapp.MakeAppointmentDocActivity
import com.example.dermapp.ManageDocLocationsActivity
import com.example.dermapp.ProfileDocActivity
import com.example.dermapp.R
import com.example.dermapp.ReportActivity
import com.example.dermapp.SetAppointmentDocActivity
import com.example.dermapp.database.AppUser
import com.example.dermapp.database.Appointment
import com.example.dermapp.database.Doctor
import com.example.dermapp.database.MedicalReport
import com.example.dermapp.database.Prescription
import com.example.dermapp.messages.MessagesDocActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StartDocActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: ImageButton
    private lateinit var navView: NavigationView
    private var currentDocId = FirebaseAuth.getInstance().currentUser?.uid
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var recyclerViewAppointments: RecyclerView
    private lateinit var recyclerViewReports: RecyclerView
    private lateinit var recyclerViewPrescriptions: RecyclerView
    private lateinit var recyclerViewArchivalAppointments: RecyclerView

    private lateinit var appointmentsAdapter: MyAdapterStartDocAppointment
    private lateinit var reportsAdapter: MyAdapterStartDocReport
    private lateinit var prescriptionsAdapter: MyAdapterStartDocPrescription
    private lateinit var archivalAdapter: MyAdapterStartDocArchivalAppointment

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_doc)

        loadCurrentDoctorId()

        drawerLayout = findViewById(R.id.drawer_layout)

        recyclerViewAppointments = findViewById(R.id.RVstartDocAppointment)
        recyclerViewAppointments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        appointmentsAdapter = MyAdapterStartDocAppointment(mutableListOf(), this)
        recyclerViewAppointments.adapter = appointmentsAdapter

        recyclerViewReports = findViewById(R.id.RVstartDocReport)
        recyclerViewReports.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        reportsAdapter = MyAdapterStartDocReport(mutableListOf(), this)
        recyclerViewReports.adapter = reportsAdapter

        recyclerViewPrescriptions = findViewById(R.id.RVstartDocPrescription)
        recyclerViewPrescriptions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        prescriptionsAdapter = MyAdapterStartDocPrescription(mutableListOf(), this)
        recyclerViewPrescriptions.adapter = prescriptionsAdapter

        recyclerViewArchivalAppointments = findViewById(R.id.RVstartDocArchival)
        recyclerViewArchivalAppointments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        archivalAdapter = MyAdapterStartDocArchivalAppointment(mutableListOf(), this)
        recyclerViewArchivalAppointments.adapter = archivalAdapter

        val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())


        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RVstartDocAppointment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RVstartDocReport)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                R.id.nav_myLocations -> {
                    val intent = Intent(this, ManageDocLocationsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_make -> {
                    val intent = Intent(this, MakeAppointmentDocActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_set -> {
                    val intent = Intent(this, SetAppointmentDocActivity::class.java)
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

                R.id.nav_myAppointments -> {
                    val intent = Intent(this, AppointmentsDocActivity::class.java)
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

    private fun loadCurrentDoctorId() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("doctors").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val doctor = documentSnapshot.toObject(Doctor::class.java)
                    currentDocId = doctor?.doctorId
                    fetchAppointments()
                    fetchPrescriptions()
                    fetchReports()
                    fetchArchivalAppointments()
                } else {
                    Toast.makeText(this, "Failed to load doctor information.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAppointments() {
        val appointmentsCollection = FirebaseFirestore.getInstance().collection("appointment")
        val today = Calendar.getInstance().time

        currentDocId?.let { uid ->
            appointmentsCollection
                .whereEqualTo("doctorId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    val appointments = mutableListOf<Appointment>()
                    for (document in documents) {
                        val appointment = document.toObject(Appointment::class.java)
                        if (appointment.datetime!! >= today) {
                            appointments.add(appointment)
                        }
                    }
                    val sortedAppointments = appointments.sortedBy { it.datetime }
                    appointmentsAdapter.updateAppointments(sortedAppointments)
                    //appointmentsAdapter.updateAppointments(appointments)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching appointments", exception)
                }
        }
    }

    private fun fetchArchivalAppointments() {
        val appointmentsCollection = FirebaseFirestore.getInstance().collection("appointment")
        val today = Calendar.getInstance().time

        currentDocId?.let { uid ->
            appointmentsCollection
                .whereEqualTo("doctorId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    val appointments = mutableListOf<Appointment>()
                    for (document in documents) {
                        val appointment = document.toObject(Appointment::class.java)
                        if (appointment.datetime!! < today) {
                            appointments.add(appointment)
                        }
                    }
                    val sortedAppointments = appointments.sortedByDescending { it.datetime }

                    archivalAdapter.updateAppointments(sortedAppointments)
                    //appointmentsAdapter.updateAppointments(appointments)
                }
                .addOnFailureListener { exception ->
                    // Handle errors
                    // For example, Log.e(TAG, "Error fetching appointments", exception)
                }
        }
    }

    private fun fetchReports() {
        val reportsCollection = FirebaseFirestore.getInstance().collection("report")

        currentDocId?.let { uid ->
            reportsCollection
                .whereEqualTo("doctorId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    val reports = mutableListOf<MedicalReport>()
                    for (document in documents) {
                        val report = document.toObject(MedicalReport::class.java)
                        reports.add(report)
                    }
                    val sortedReports = reports.sortedBy {
                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(it.date)
                    }

                    reportsAdapter.updateReports(sortedReports)
                    //reportsAdapter.updateReports(reports)
                }
                .addOnFailureListener { exception ->
                    // Handle errors
                    // For example, Log.e(TAG, "Error fetching prescriptions", exception)
                }
        }
    }

    private fun fetchPrescriptions() {
        val prescriptionsCollection = FirebaseFirestore.getInstance().collection("prescription")

        currentDocId?.let { uid ->
            prescriptionsCollection
                .whereEqualTo("doctorId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    val prescriptions = mutableListOf<Prescription>()
                    for (document in documents) {
                        val prescription = document.toObject(Prescription::class.java)
                        prescriptions.add(prescription)
                    }
                    val sortedPrescriptions = prescriptions.sortedByDescending { it.date }

                    prescriptionsAdapter.updatePrescriptions(sortedPrescriptions)
                    //prescriptionsAdapter.updatePrescriptions(prescriptions)
                }
                .addOnFailureListener { exception ->
                    // Handle errors
                    // For example, Log.e(TAG, "Error fetching prescriptions", exception)
                }
        }
    }
}
