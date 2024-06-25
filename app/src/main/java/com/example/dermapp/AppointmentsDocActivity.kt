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
 */
class AppointmentsDocActivity : AppCompatActivity() {

    // Declare UI elements
    private lateinit var backButton: ImageButton
    private lateinit var calendarView: CalendarView
    private lateinit var appointmentsListView: ListView

    // Firestore instance
    private val firestore = FirebaseFirestore.getInstance()
    // List to store appointments
    private val appointments = mutableListOf<Appointment>()
    // Date format for displaying dates
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    // Current doctor's ID
    private lateinit var currentDoctorId : String
    // Map to cache patient information
    private val patientsMap = mutableMapOf<String, Patient>()

    /**
     * Called when the activity is starting.
     * Sets up UI elements, initializes listeners, and retrieves necessary data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointments_doc)

        backButton = findViewById(R.id.arrowButton)
        calendarView = findViewById(R.id.calendarView)
        appointmentsListView = findViewById(R.id.appointmentsListView)

        // Set click listener for back button
        backButton.setOnClickListener {
            startActivity(Intent(this, StartDocActivity::class.java))
        }

        // Load current doctor's ID
        loadDoctorId()

        // Set date change listener for calendar view
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            displayAppointmentsForDate(selectedDate.time)
        }
    }

    /**
     * Load the ID of the current doctor from Firestore.
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
     * Load appointments for the current doctor from Firestore.
     */
    private fun loadAppointments() {
        if (currentDoctorId != null) {
            firestore.collection("appointment")
                .whereEqualTo("doctorId", currentDoctorId)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val appointment = document.toObject(Appointment::class.java)
                        appointments.add(appointment)
                        Log.d(TAG, "APPOINTMENT ${appointment}")
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    /**
     * Display appointments for the selected date.
     *
     * @param date The selected date.
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
                    // All appointments processed, update UI
                    val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, appointmentDetails)
                    appointmentsListView.adapter = adapter
                }
            }
        }
    }

    /**
     * Load patient information from Firestore.
     *
     * @param patientId The ID of the patient.
     * @param callback The callback function to execute after patient info is loaded.
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