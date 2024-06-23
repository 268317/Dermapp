package com.example.dermapp.startPatient

import MyAdapterStartPatReport
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.CreateNewReportActivity
import com.example.dermapp.MainActivity
import com.example.dermapp.MakeAppointmentPatActivity
import com.example.dermapp.ProfilePatActivity
import com.example.dermapp.R
import com.example.dermapp.ReportActivity
import com.example.dermapp.database.AppUser
import com.example.dermapp.database.Appointment
import com.example.dermapp.database.MedicalReport
import com.example.dermapp.database.Prescription
import com.example.dermapp.messages.MessagesPatActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class StartPatActivity : AppCompatActivity(), MyAdapterStartPatReport.OnItemClickListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: ImageButton
    private lateinit var navView: NavigationView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_patient)


        drawerLayout = findViewById(R.id.drawer_layout)

        val recyclerViewAppointments = findViewById<RecyclerView>(R.id.RVstartPatAppointment)
        recyclerViewAppointments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val appointments: MutableList<Appointment> = ArrayList()

        val recyclerViewReports = findViewById<RecyclerView>(R.id.RVstartPatReport)
        recyclerViewReports.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val reports: MutableList<MedicalReport> = ArrayList()

        val recyclerViewPrescriptions = findViewById<RecyclerView>(R.id.RVstartPatPrescription)
        recyclerViewPrescriptions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val prescriptions: MutableList<Prescription> = ArrayList()

        appointments.add(
            Appointment(
                doctorId = "Jan",
                patientId = "Kowalski",
                appointmentDate = Date()
            )
        )

        appointments.add(
            Appointment(
                doctorId = "Adam",
                patientId = "Nowak",
                appointmentDate = Date()
            )
        )

        appointments.add(
            Appointment(
                doctorId = "Monika",
                patientId = "Adamska",
                appointmentDate = Date()
            )
        )

        appointments.add(
            Appointment(
                doctorId = "Anna",
                patientId = "Kwiatek",
                appointmentDate = Date()
            )
        )

        reports.add(
            MedicalReport(
                medicalReportId = "1jstXDBrYMZppA30ZZjl",
                doctorId = "12345",
                patientPesel = "83627837264",
                reportDate = "22-06-2024 17:55",
                attachmentUrl = "content://media/external/images/media/1000035185"
            )
        )

        reports.add(
            MedicalReport(
                doctorId = "Jan",
                patientPesel = "Kowalski",
                reportDate = "10-06-2024"
            )
        )

        reports.add(
            MedicalReport(
                doctorId = "Jan",
                patientPesel = "Kowalski",
                reportDate = "10-06-2024"
            )
        )

        prescriptions.add(
            Prescription(
                doctorId = "Jan",
                patientPesel = "Kowalski",
                prescriptionDate = "10-06-2024"
            )
        )

        prescriptions.add(
            Prescription(
                doctorId = "Jan",
                patientPesel = "Kowalski",
                prescriptionDate = "10-06-2024"
            )
        )

        prescriptions.add(
            Prescription(
                doctorId = "Jan",
                patientPesel = "Kowalski",
                prescriptionDate = "10-06-2024"
            )
        )

        // Set layout manager and adapter for RecyclerView
        recyclerViewAppointments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewAppointments.adapter = MyAdapterStartPatAppointment(appointments)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RVstartPatAppointment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set layout manager and adapter for RecyclerView
        recyclerViewReports.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewReports.adapter = MyAdapterStartPatReport(reports, this)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RVstartPatReport)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set layout manager and adapter for RecyclerView
        recyclerViewPrescriptions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewPrescriptions.adapter = MyAdapterStartPatPrescription(prescriptions)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RVstartPatPrescription)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                R.id.nav_myAppointments -> {
                    Toast.makeText(this, "My appointments clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_newReport -> {
                    val intent = Intent(this, CreateNewReportActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_myPrescriptions -> {
                    Toast.makeText(this, "My prescriptions clicked", Toast.LENGTH_SHORT).show()
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

    override fun onItemClick(medicalReportId: String) {
        // Start ReportActivity and pass medicalReportId
        val intent = Intent(this, ReportActivity::class.java)
        intent.putExtra(ReportActivity.MEDICAL_REPORT_ID_EXTRA, medicalReportId)
        startActivity(intent)
    }
}
