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

class AppointmentsDocActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var calendarView: CalendarView
    private lateinit var appointmentsListView: ListView

    private val firestore = FirebaseFirestore.getInstance()
    private val appointments = mutableListOf<Appointment>()
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private lateinit var currentDoctorId : String
    private val patientsMap = mutableMapOf<String, Patient>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointments_doc)

        backButton = findViewById(R.id.arrowButton)
        calendarView = findViewById(R.id.calendarView)
        appointmentsListView = findViewById(R.id.appointmentsListView)

        backButton.setOnClickListener {
            startActivity(Intent(this, StartDocActivity::class.java))
        }

        loadDoctorId()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            displayAppointmentsForDate(selectedDate.time)
        }
    }

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

    private fun displayAppointmentsForDate(date: Date) {
        val appointmentDetails = mutableListOf<String>()
        appointmentsListView.adapter = null
        patientsMap.clear()
        val selectedDateAppointments = appointments.filter { appointment ->
            appointment.appointmentDate?.let { dateFormat.format(it) } == dateFormat.format(date)
        }


        val appointmentCount = selectedDateAppointments.size
        var appointmentsProcessed = 0

        selectedDateAppointments.forEach { appointment ->
            val appointmentDate = appointment.appointmentDate
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