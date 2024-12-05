package com.example.dermapp

import android.app.AlarmManager
import android.app.AlertDialog
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
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Activity that allows a patient to schedule an appointment with a doctor.
 * It handles doctor selection, location selection, date/time selection, and appointment booking.
 */
class MakeAppointmentPatActivity : AppCompatActivity() {

    // UI elements for user interaction
    private lateinit var backButton: ImageButton
    private lateinit var autoDateTime: AutoCompleteTextView
    private lateinit var autoDoc: AutoCompleteTextView
    private lateinit var autoLoc: AutoCompleteTextView
    private lateinit var bookButton: Button

    // Selected IDs for doctor, location, and datetime
    private var selectedDateTimeId: String? = null
    private var selectedDoctorId: String? = null
    private var selectedLocationId: String? = null

    // Firebase Firestore instance for database operations
    private val firestore = FirebaseFirestore.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    /**
     * Called when the activity is starting.
     * Initializes UI elements, sets up listeners, and loads data from Firestore.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_appointment_pat)

        // Set time zone to Europe/Warsaw for date/time operations
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        // Initialize UI elements
        autoDateTime = findViewById(R.id.autoCompleteTextDate)
        autoDoc = findViewById(R.id.autoCompleteTextViewDoctor)
        autoLoc = findViewById(R.id.autoCompleteTextViewLocalization)
        bookButton = findViewById(R.id.bookButton)

        // Set up back button to navigate to the previous screen
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, StartPatActivity::class.java))
        }

        // Set listener for selecting date/time
        autoDateTime.setOnClickListener {
            if (selectedDoctorId != null && selectedLocationId != null) {
                loadDoctorAvailableDatetime(selectedDoctorId!!, selectedLocationId!!)
            } else {
                Toast.makeText(
                    this,
                    "Please select a doctor and location first.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Set up auto-complete text views for doctor and location selection
        setupAutoCompleteTextView(autoDoc)
        setupAutoCompleteTextView(autoLoc)

        // Load initial doctor data
        loadDoctors()

        // Set listener for booking the appointment
        bookButton.setOnClickListener {
            bookAppointment()
        }
    }

    /**
     * Sets up the auto-complete text view configuration.
     * @param autoCompleteTextView The AutoCompleteTextView to set up.
     */
    private fun setupAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView) {
        autoCompleteTextView.inputType = 0
        autoCompleteTextView.isFocusable = false
        autoCompleteTextView.isClickable = true
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }
    }

    /**
     * Loads the list of doctors from Firestore and sets up the auto-complete adapter.
     */
    private fun loadDoctors() {
        val doctorsCollection = firestore.collection("doctors")
        doctorsCollection.get()
            .addOnSuccessListener { doctorsResult ->
                val doctorsList = doctorsResult.toObjects(Doctor::class.java)
                val doctorNames =
                    doctorsList.map { "${it.firstName} ${it.lastName}" }.toTypedArray()
                val docAdapter =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, doctorNames)
                autoDoc.setAdapter(docAdapter)
                autoDoc.setOnItemClickListener { _, _, position, _ ->
                    selectedDoctorId = doctorsList[position].doctorId
                    loadDoctorLocations(selectedDoctorId!!)
                }
                // Set up auto-complete for selecting doctor
                autoDoc.setOnItemClickListener { _, _, position, _ ->
                    selectedDoctorId = doctorsList[position].doctorId

                    // Reset location and datetime when doctor is changed
                    selectedLocationId = null
                    autoLoc.setText("") // Clear the location field
                    autoDateTime.setText("") // Clear the datetime field

                    // Load locations based on selected doctor
                    loadDoctorLocations(selectedDoctorId!!)
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    /**
     * Loads the locations associated with the given doctor ID from Firestore and sets up the auto-complete adapter.
     * @param doctorId The ID of the doctor whose locations are to be loaded.
     */
    private fun loadDoctorLocations(doctorId: String) {
        val locationsCollection = firestore.collection("locations")
        locationsCollection.whereEqualTo("doctorId", doctorId).get()
            .addOnSuccessListener { locationsResult ->
                val locList = locationsResult.toObjects(Location::class.java)
                val locNames = locList.map { it.fullAddress }.toTypedArray()
                if (locNames.isNotEmpty()) {
                    val locAdapter =
                        ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locNames)
                    autoLoc.setAdapter(locAdapter)
                    autoLoc.setOnItemClickListener { _, _, position, _ ->
                        selectedLocationId = locList[position].locationId
                    }
                    autoLoc.showDropDown()
                } else {
                    Toast.makeText(
                        this,
                        "No locations available for the selected doctor.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                autoLoc.setOnItemClickListener { _, _, position, _ ->
                    selectedLocationId = locList[position].locationId

                    // Reset datetime when location is changed
                    selectedDateTimeId = null
                    autoDateTime.setText("") // Clear the datetime field
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    /**
     * Loads the available date/time slots for the given doctor and location from Firestore and sets up the auto-complete adapter.
     * @param doctorId The ID of the doctor.
     * @param locationId The ID of the location.
     */
    private fun loadDoctorAvailableDatetime(doctorId: String, locationId: String) {
        val now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw")).time // Ensure current time is in Warsaw timezone
        coroutineScope.launch {
            try {
                val availableDatesCollection = firestore.collection("availableDates")
                    .whereEqualTo("doctorId", doctorId)
                    .whereEqualTo("locationId", locationId)
                    .whereEqualTo("isAvailable", true)
                    .whereGreaterThan("datetime", now)
                    .get()
                    .await()

                val availableDates = availableDatesCollection.toObjects(AvailableDates::class.java)

                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw")

                val dateTimes = availableDates.map { availableDate ->
                    dateFormat.format(availableDate.datetime)
                }.toTypedArray()

                if (dateTimes.isNotEmpty()) {
                    val dateTimeAdapter = ArrayAdapter(
                        this@MakeAppointmentPatActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        dateTimes
                    )
                    autoDateTime.setAdapter(dateTimeAdapter)
                    autoDateTime.setOnItemClickListener { _, _, position, _ ->
                        selectedDateTimeId = availableDates[position].availableDateId
                    }
                    autoDateTime.showDropDown()
                } else {
                    Toast.makeText(
                        this@MakeAppointmentPatActivity,
                        "No available datetimes for the selected doctor and location.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@MakeAppointmentPatActivity,
                    "Failed to load available datetimes: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Books the appointment by saving it to Firestore.
     */
    private fun bookAppointment() {
        val doctorId = selectedDoctorId ?: return
        val locationId = selectedLocationId ?: return
        val dateTimeId = selectedDateTimeId ?: return

        val doctor = autoDoc.text.toString()
        val location = autoLoc.text.toString()
        val dateTime = autoDateTime.text.toString()

        if (doctor.isNotEmpty() && location.isNotEmpty() && dateTime.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw") // Ensure date format uses Warsaw timezone

                val appointmentDate = dateFormat.parse(dateTime)
                val appointmentTimeInMillis = appointmentDate?.time ?: throw ParseException("Invalid date format", 0)

                val appointment = Appointment(
                    doctorId = doctorId,
                    patientId = FirebaseAuth.getInstance().currentUser?.uid,
                    datetime = appointmentDate,
                    localization = location,
                    diagnosis = "", // Set diagnosis if applicable
                    recommendations = "" // Set recommendations if applicable
                )

                firestore.collection("appointment")
                    .add(appointment)
                    .addOnSuccessListener { documentReference ->
                        val generatedAppointmentId = documentReference.id

                        val updatedAppointment = appointment.copy(appointmentId = generatedAppointmentId)

                        documentReference.set(updatedAppointment)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Appointment booked successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val availableDatesRef = firestore.collection("availableDates").document(dateTimeId)
                                availableDatesRef.update("isAvailable", false)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Availability updated successfully.",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        setAppointmentReminder(generatedAppointmentId, appointmentTimeInMillis, location)

                                        // Prompt for another appointment
                                        showBookingPrompt()

                                        // Reset fields
                                        autoDoc.setText("")
                                        autoLoc.setText("")
                                        autoDateTime.setText("")
                                        selectedDoctorId = null
                                        selectedLocationId = null
                                        selectedDateTimeId = null
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Failed to update availability: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Failed to set appointmentId: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Failed to book appointment: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

            } catch (e: ParseException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to parse date: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shows a dialog asking if the user wants to book another appointment.
     */
    private fun showBookingPrompt() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to book another appointment?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog
                // Stay in the same view to book another appointment
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog
                // Navigate back to the main screen
                startActivity(Intent(this, StartPatActivity::class.java))
                finish() // Optional: close this activity
            }

        val alert = builder.create()
        alert.show()
    }

    /**
     * Sets a reminder for the scheduled appointment 24 hours in advance.
     * @param appointmentId The ID of the appointment.
     * @param appointmentTimeInMillis The time of the appointment in milliseconds.
     * @param location The location of the appointment.
     */
    private fun setAppointmentReminder(appointmentId: String, appointmentTimeInMillis: Long, location: String) {
        val intent = Intent(this, ReminderBroadcast::class.java)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val reminderTimeInMillis = appointmentTimeInMillis - 24 * 60 * 60 * 1000 // 24 hours before

        Log.d("setAppointmentReminder", "Setting reminder for appointmentId: $appointmentId at time: $reminderTimeInMillis")

        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, pendingIntent)

        // For testing purposes, set an additional reminder after 10 seconds
        val currentTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw")).timeInMillis // Ensure current time is in Warsaw timezone
        val tenSecondsMillis = 10000 // 10 seconds in milliseconds

        alarmManager.set(AlarmManager.RTC_WAKEUP, currentTime + tenSecondsMillis, pendingIntent)
    }
}
