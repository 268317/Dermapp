package com.example.dermapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.database.Appointment
import com.example.dermapp.database.Doctor
import com.example.dermapp.database.Patient
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity to manage and display appointments for doctors.
 * Allows doctors to view their appointments by selecting a specific date from the calendar.
 * Displays patient information and appointment time for the selected date.
 */
class AppointmentsDocActivity : AppCompatActivity() {

    // Declare UI elements for the activity
    private lateinit var backButton: ImageButton
    private lateinit var calendarView: CalendarView
    private lateinit var appointmentsListView: ListView

    // Firestore instance for database operations
    private val firestore = FirebaseFirestore.getInstance()
    // List to store appointment data
    private val appointments = mutableListOf<Appointment>()
    // Date format used for displaying appointment dates
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    // ID of the currently logged-in doctor
    private lateinit var currentDoctorId: String
    // Map to cache patient information for faster access
    private val patientsMap = mutableMapOf<String, Patient>()

    /**
     * Called when the activity is starting.
     * Sets up the UI, initializes event listeners, and loads data from Firestore.
     *
     * @param savedInstanceState Contains data it most recently supplied in `onSaveInstanceState`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointments_doc)

        // Set the timezone to Warsaw for consistent time handling
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        // Initialize UI elements by linking them to their respective IDs
        backButton = findViewById(R.id.arrowButton)
        calendarView = findViewById(R.id.calendarView)
        appointmentsListView = findViewById(R.id.appointmentsListView)

        // Set a click listener for the back button to navigate back to the start screen
        backButton.setOnClickListener {
            startActivity(Intent(this, StartDocActivity::class.java))
        }

        // Load the ID of the currently logged-in doctor
        loadDoctorId()

        // Set a date change listener on the calendar view to update appointments for the selected date
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            displayAppointmentsForDate(selectedDate.time)
        }
    }

    /**
     * Load the ID of the current doctor from Firestore.
     * Fetches the doctor's information and initializes the appointments list.
     */
    private fun loadDoctorId() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("doctors").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val doctor = documentSnapshot.toObject(Doctor::class.java)
                    currentDoctorId = doctor?.doctorId.toString()
                    loadAppointments()
                } else {
                    Toast.makeText(this, "Failed to load doctor information.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Load all appointments for the current doctor from Firestore.
     * Appointments are stored in a list for further filtering and display.
     */
    private fun loadAppointments() {
        if (::currentDoctorId.isInitialized) {
            firestore.collection("appointment")
                .whereEqualTo("doctorId", currentDoctorId)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val appointment = document.toObject(Appointment::class.java)
                        appointments.add(appointment)
                        Log.d(TAG, "APPOINTMENT: $appointment")
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    /**
     * Display appointments for the selected date.
     * Filters appointments based on the selected date and updates the ListView.
     *
     * @param date The date for which appointments should be displayed.
     */
    private fun displayAppointmentsForDate(date: Date) {
        val appointmentDetails = mutableListOf<String>()
        appointmentsListView.adapter = null
        patientsMap.clear()
        val selectedDateAppointments = appointments.filter { appointment ->
            appointment.datetime?.let { dateFormat.format(it) } == dateFormat.format(date)
        }

        val appointmentCount = selectedDateAppointments.size
        var appointmentsProcessed = 0

        selectedDateAppointments.forEach { appointment ->
            val appointmentDate = appointment.datetime
            val time = appointmentDate?.let {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
            }
            val patientId = appointment.patientId

            loadPatientInfo(patientId) { patient ->
                val patientFirstName = patient?.firstName ?: "Firstname"
                val patientLastName = patient?.lastName ?: "Lastname"

                val appointmentDetail = "$time $patientFirstName $patientLastName"
                appointmentDetails.add(appointmentDetail)

                appointmentsProcessed++
                if (appointmentsProcessed == appointmentCount) {
                    // All appointments processed, update the ListView with the data
                    val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, appointmentDetails)
                    appointmentsListView.adapter = adapter
                }
            }
        }
    }

    /**
     * Load patient information from Firestore.
     * Uses a callback to return the patient data after loading.
     *
     * @param patientId The ID of the patient whose data needs to be fetched.
     * @param callback A function to execute after patient data is loaded.
     */
    private fun loadPatientInfo(patientId: String?, callback: (Patient?) -> Unit) {
        if (patientId != null && patientsMap.containsKey(patientId)) {
            callback(patientsMap[patientId])
        } else {
            if (patientId != null) {
                firestore.collection("patients").document(patientId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val patient = documentSnapshot.toObject(Patient::class.java)
                            if (patient != null) {
                                patientsMap[patientId] = patient
                                callback(patient)
                            } else {
                                callback(null)
                            }
                        } else {
                            callback(null)
                        }
                    }
                    .addOnFailureListener { exception ->
                        exception.printStackTrace()
                        callback(null)
                    }
            }
        }
    }
}
