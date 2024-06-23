package com.example.dermapp

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.database.Appointment
import com.example.dermapp.database.AvailableDates
import com.example.dermapp.database.Doctor
import com.example.dermapp.database.Location
import com.example.dermapp.database.Patient
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MakeAppointmentDocActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var autoDateTime: AutoCompleteTextView
    private lateinit var autoPat: AutoCompleteTextView
    private lateinit var autoLoc: AutoCompleteTextView
    private lateinit var bookButton: Button

    private var selectedDateTimeId: String? = null
    private var selectedPatientId: String? = null
    private var selectedLocationId: String? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var currentDocId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_appointment_doc)

        autoDateTime = findViewById(R.id.autoCompleteTextDate)
        autoPat = findViewById(R.id.autoCompleteTextViewPatient)
        autoLoc = findViewById(R.id.autoCompleteTextViewLocalization)
        bookButton = findViewById(R.id.bookButton)

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            startActivity(Intent(this, StartDocActivity::class.java))
        }

        autoDateTime.setOnClickListener {
            if (selectedPatientId != null && selectedLocationId != null) {
                loadDoctorAvailableDatetime(currentDocId!!, selectedLocationId!!)
            } else {
                Toast.makeText(this, "Please select a patient and location first.", Toast.LENGTH_SHORT).show()
            }
        }

        setupAutoCompleteTextView(autoPat)
        setupAutoCompleteTextView(autoLoc)

        loadCurrentDoctorId()
        loadPatients()

        bookButton.setOnClickListener {
            bookAppointment()
        }
    }

    private fun setupAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView) {
        autoCompleteTextView.inputType = 0
        autoCompleteTextView.isFocusable = false
        autoCompleteTextView.isClickable = true
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }
    }

    private fun loadCurrentDoctorId() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("doctors").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val doctor = documentSnapshot.toObject(Doctor::class.java)
                    currentDocId = doctor?.doctorId
                } else {
                    Toast.makeText(this, "Failed to load doctor information.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun loadPatients() {
        val patientsCollection = firestore.collection("patients")
        patientsCollection.get()
            .addOnSuccessListener { patientsResult ->
                val patientsList = patientsResult.toObjects(Patient::class.java)
                val patientNames = patientsList.map { "${it.firstName} ${it.lastName}" }.toTypedArray()
                val patAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, patientNames)
                autoPat.setAdapter(patAdapter)
                autoPat.setOnItemClickListener { _, _, position, _ ->
                    selectedPatientId = patientsList[position].appUserId
                    loadDoctorLocations(currentDocId!!)
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun loadDoctorLocations(doctorId: String) {
        val locationsCollection = firestore.collection("locations")
        locationsCollection.whereEqualTo("doctorId", doctorId).get()
            .addOnSuccessListener { locationsResult ->
                val locList = locationsResult.toObjects(Location::class.java)
                val locNames = locList.map { it.fullAddress }.toTypedArray()
                if (locNames.isNotEmpty()) {
                    val locAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locNames)
                    autoLoc.setAdapter(locAdapter)
                    autoLoc.setOnItemClickListener { _, _, position, _ ->
                        selectedLocationId = locList[position].locationId
                    }
                    autoLoc.showDropDown()
                } else {
                    Toast.makeText(this, "No locations available for the selected doctor.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun loadDoctorAvailableDatetime(doctorId: String, locationId: String) {
        coroutineScope.launch {
            try {
                val availableDatesCollection = firestore.collection("availableDates")
                    .whereEqualTo("doctorId", doctorId)
                    .whereEqualTo("locationId", locationId)
                    .whereEqualTo("isAvailable", true)
                    .get()
                    .await()

                val availableDates = availableDatesCollection.toObjects(AvailableDates::class.java)

                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw")

                val dateTimes = availableDates.map { availableDate ->
                    dateFormat.format(availableDate.datetime)
                }.toTypedArray()

                if (dateTimes.isNotEmpty()) {
                    val dateTimeAdapter = ArrayAdapter(this@MakeAppointmentDocActivity, android.R.layout.simple_dropdown_item_1line, dateTimes)
                    autoDateTime.setAdapter(dateTimeAdapter)
                    autoDateTime.setOnItemClickListener { _, _, position, _ ->
                        selectedDateTimeId = availableDates[position].availableDateId
                    }
                    autoDateTime.showDropDown()
                } else {
                    Toast.makeText(this@MakeAppointmentDocActivity, "No available datetimes for the selected doctor and location.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MakeAppointmentDocActivity, "Failed to load available datetimes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bookAppointment() {
        val doctorId = currentDocId ?: return
        val locationId = selectedLocationId ?: return
        val dateTimeId = selectedDateTimeId ?: return

        val patient = autoPat.text.toString()
        val location = autoLoc.text.toString()
        val dateTime = autoDateTime.text.toString()

        if (patient.isNotEmpty() && location.isNotEmpty() && dateTime.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw")

                val appointmentDate = dateFormat.parse(dateTime)
                val appointmentTimeInMillis = appointmentDate?.time ?: throw ParseException("Invalid date format", 0)

                val appointment = Appointment(
                    doctorId = doctorId,
                    patientId = selectedPatientId,
                    appointmentDate = appointmentDate,
                    localization = location,
                    diagnosis = "",
                    recommendations = ""
                )

                firestore.collection("appointment")
                    .add(appointment)
                    .addOnSuccessListener { documentReference ->
                        val generatedAppointmentId = documentReference.id

                        val updatedAppointment = appointment.copy(appointmentId = generatedAppointmentId)

                        documentReference.set(updatedAppointment)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Appointment booked successfully.", Toast.LENGTH_SHORT).show()

                                val generatedAppointmentId = documentReference.id

                                val availableDatesRef = firestore.collection("availableDates").document(dateTimeId)
                                availableDatesRef
                                    .update("isAvailable", false)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Availability updated successfully.", Toast.LENGTH_SHORT).show()

                                        setAppointmentReminder(generatedAppointmentId, appointmentTimeInMillis, location)

                                        autoPat.setText("")
                                        autoLoc.setText("")
                                        autoDateTime.setText("")
                                        selectedPatientId = null
                                        selectedLocationId = null
                                        selectedDateTimeId = null
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Failed to update availability: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to set appointmentId: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to book appointment: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            } catch (e: ParseException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to parse date: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setAppointmentReminder(appointmentId: String, appointmentTimeInMillis: Long, location: String) {
        val intent = Intent(this, ReminderBroadcast::class.java)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val reminderTimeInMillis = appointmentTimeInMillis - 24 * 60 * 60 * 1000

        Log.d("setAppointmentReminder", "Setting reminder for appointmentId: $appointmentId at time: $reminderTimeInMillis")

        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, pendingIntent)
    }
}