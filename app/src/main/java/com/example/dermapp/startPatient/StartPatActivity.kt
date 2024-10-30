package com.example.dermapp.startPatient

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
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
import com.example.dermapp.ManageDocLocationsActivity
import com.example.dermapp.ProfileDocActivity
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity for patients to view their appointments, reports, prescriptions,
 * and manage other functionalities like profile and messaging.
 */
class StartPatActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: ImageButton
    private lateinit var navView: NavigationView

    private lateinit var startButtonPat1: ImageView
    private lateinit var startButtonPat2: ImageView
    private lateinit var startButtonPat3: ImageView

    private lateinit var recyclerViewAppointments: RecyclerView
    private lateinit var recyclerViewReports: RecyclerView
    private lateinit var recyclerViewArchivalAppointments: RecyclerView

    private lateinit var appointmentsAdapter: MyAdapterStartPatAppointment
    private lateinit var reportsAdapter: MyAdapterStartPatReport
    private lateinit var archivalAdapter: MyAdapterStartPatArchivalAppointment

    /**
     * Initializes the activity, sets up UI elements, and fetches necessary data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_patient)

        // Initialize DrawerLayout and its components
        drawerLayout = findViewById(R.id.drawer_layout)

        // Initialize RecyclerViews and adapters for appointments, reports, prescriptions, and archival appointments
        recyclerViewAppointments = findViewById(R.id.RVstartPatAppointment)
        recyclerViewAppointments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        appointmentsAdapter = MyAdapterStartPatAppointment(mutableListOf(), this)
        recyclerViewAppointments.adapter = appointmentsAdapter

        recyclerViewReports = findViewById(R.id.RVstartPatReport)
        recyclerViewReports.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        reportsAdapter = MyAdapterStartPatReport(mutableListOf(), this)
        recyclerViewReports.adapter = reportsAdapter

//        recyclerViewPrescriptions = findViewById(R.id.RVstartPatPrescription)
//        recyclerViewPrescriptions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        prescriptionsAdapter = MyAdapterStartPatPrescription(mutableListOf(), this)
//        recyclerViewPrescriptions.adapter = prescriptionsAdapter

        recyclerViewArchivalAppointments = findViewById(R.id.RVstartPatArchivalAppointments)
        recyclerViewArchivalAppointments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        archivalAdapter = MyAdapterStartPatArchivalAppointment(mutableListOf(), this)
        recyclerViewArchivalAppointments.adapter = archivalAdapter

        startButtonPat1 = findViewById(R.id.startButtonPat1)
        startButtonPat1.setOnClickListener {
            val intent = Intent(this, ProfilePatActivity::class.java)
            startActivity(intent)
        }

        startButtonPat2 = findViewById(R.id.startButtonPat2)
        startButtonPat2.setOnClickListener {
            val intent = Intent(this, MakeAppointmentPatActivity::class.java)
            startActivity(intent)
        }

        startButtonPat3 = findViewById(R.id.startButtonPat3)
        startButtonPat3.setOnClickListener {
            val intent = Intent(this, CreateNewReportActivity::class.java)
            startActivity(intent)
        }

        // Initialize menu button in the header to open navigation drawer
        val header = findViewById<RelativeLayout>(R.id.includeHeader)
        menuButton = header.findViewById(R.id.menuButton)

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Set navigation item click listener for the navigation drawer
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

        // Fetch data for appointments, reports, prescriptions, and archival appointments
        fetchAppointments()
        fetchReports()
//        fetchPrescriptions()
        fetchArchivalAppointments()


        // Fetch current user's data and display their first name in the header
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserUid!!)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(AppUser::class.java)

                user?.let {
                    val headerNameTextView: TextView = findViewById(R.id.firstNameTextView)
                    headerNameTextView.text = user.firstName
                }
            }
        }


    }

    /**
     * Fetches upcoming appointments for the current patient from Firestore.
     */
    private fun fetchAppointments() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val appointmentsCollection = FirebaseFirestore.getInstance().collection("appointment")
        val today = Calendar.getInstance().time

        currentUserUid?.let { uid ->
            appointmentsCollection
                .whereEqualTo("patientId", uid)
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
        }
    }

    /**
     * Fetches past appointments (archival) for the current patient from Firestore.
     */
    private fun fetchArchivalAppointments() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val appointmentsCollection = FirebaseFirestore.getInstance().collection("appointment")
        val today = Calendar.getInstance().time

        currentUserUid?.let { uid ->
            appointmentsCollection
                .whereEqualTo("patientId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    val appointments = mutableListOf<Appointment>()
                    for (document in documents) {
                        val appointment = document.toObject(Appointment::class.java)
                        if (appointment.datetime!! < today) {
                            appointments.add(appointment)
                        }
                    }
                    val sortedAppointments = appointments.sortedBy { it.datetime }

                    archivalAdapter.updateAppointments(sortedAppointments)
                    //appointmentsAdapter.updateAppointments(appointments)
                }
        }
    }

    /**
     * Fetches medical reports for the current patient from Firestore.
     */
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
                            val sortedReports = reports.sortedByDescending {
                                SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).parse(it.date)
                            }

                            reportsAdapter.updateReports(sortedReports)
                                //reportsAdapter.updateReports(reports)
                        }
                } ?: run {
                }
            }
        }
    }

//    /**
//     * Fetches prescriptions for the current patient from Firestore.
//     */
//    private fun fetchPrescriptions() {
//        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
//        val prescriptionsCollection = FirebaseFirestore.getInstance().collection("prescription")
//
//        currentUserUid?.let { uid ->
//            prescriptionsCollection
//                .whereEqualTo("patientId", uid)
//                .get()
//                .addOnSuccessListener { documents ->
//                    val prescriptions = mutableListOf<Prescription>()
//                    for (document in documents) {
//                        val prescription = document.toObject(Prescription::class.java)
//                        prescriptions.add(prescription)
//                    }
//                    val sortedPrescriptions = prescriptions.sortedByDescending { it.date }
//
////                    prescriptionsAdapter.updatePrescriptions(sortedPrescriptions)
//                    //prescriptionsAdapter.updatePrescriptions(prescriptions)
//                }
//        }
//    }
}
