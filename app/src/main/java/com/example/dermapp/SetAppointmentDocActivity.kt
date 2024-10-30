package com.example.dermapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dermapp.database.AppUser
import com.example.dermapp.database.AvailableDates
import com.example.dermapp.database.Doctor
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DateTime
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Activity for doctors to set appointments with patients.
 */
class SetAppointmentDocActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var firestore: FirebaseFirestore
    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText
    private lateinit var bookButton: Button
    private lateinit var autoLoc: AutoCompleteTextView
    private var selectedLocationId: String? = null
    private var selectedDate: Date? = null
    private var selectedTime: String? = null

    /**
     * Initializes UI components and sets up listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_appointment_doc)

        // Ustawienie strefy czasowej dla aktywności
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        // Initialize Firestore instance and UI components
        firestore = FirebaseFirestore.getInstance()
        editTextDate = findViewById(R.id.editTextDate)
        autoLoc = findViewById(R.id.autoCompleteTextViewLocalization)
        bookButton = findViewById(R.id.bookButton)

        // Retrieve current user's UID
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Get doctor details from Firestore based on UID
        val userRef = FirebaseFirestore.getInstance().collection("doctors").document(currentUserUid!!)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(Doctor::class.java)

                // Load doctor's available locations and set up booking button
                user?.let {
                    loadDoctorLocations(user.doctorId)
                    val doctorId = user.doctorId

                    bookButton.setOnClickListener {
                        bookAppointment(doctorId)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            // Handle errors fetching user data from Firestore
        }

        // Setup autocomplete for location input
        setupAutoCompleteTextView(autoLoc)

        // Handle date selection using DatePickerDialog
        editTextDate.setOnClickListener {
            showDateTimePicker()
        }

        // Set up back button to return to previous activity
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Loads the locations associated with the doctor from Firestore.
     * @param doctorId The ID of the doctor to load locations for.
     */
    private fun loadDoctorLocations(doctorId: String) {
        val locationsCollection = firestore.collection("locations")
        locationsCollection.whereEqualTo("doctorId", doctorId).get()
            .addOnSuccessListener { locationsResult ->
                val locList = locationsResult.toObjects(com.example.dermapp.database.Location::class.java)
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
                // Handle failures in loading locations
                exception.printStackTrace()
            }
    }

    /**
     * Shows a date and time picker dialog to select appointment date and time.
     */
    private fun showDateTimePicker() {
        val now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw"))

        val datePicker = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = Calendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw"))
            selectedDate.set(year, monthOfYear, dayOfMonth)

            val isToday = now.get(Calendar.YEAR) == year &&
                    now.get(Calendar.MONTH) == monthOfYear &&
                    now.get(Calendar.DAY_OF_MONTH) == dayOfMonth

            val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)
                selectedDate.set(Calendar.SECOND, 0)

                // Use a formatter with explicit time zone for display
                val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                dateTimeFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw")
                editTextDate.setText(dateTimeFormat.format(selectedDate.time))

                // Store the selected date and time
                this.selectedDate = selectedDate.time
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true)

            // Adjust time picker if date is today
            if (isToday) {
                timePicker.updateTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
            }

            timePicker.show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))

        // Set minimum date for date picker as today
        datePicker.datePicker.minDate = now.timeInMillis
        datePicker.show()
    }

    /**
     * Handles booking of an appointment based on user input.
     * @param doctorId The ID of the doctor booking the appointment.
     */
    private fun bookAppointment(doctorId: String) {
        if (selectedLocationId == null || selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate start and end times for the appointment window
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        val timeParts = selectedTime!!.split(":")
        calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
        calendar.set(Calendar.MINUTE, timeParts[1].toInt())

        val startTime = calendar.time
        calendar.add(Calendar.MINUTE, -9)
        val startTimeWindow = calendar.time
        calendar.add(Calendar.MINUTE, 18) // 9 minutes before and after
        val endTimeWindow = calendar.time

        // Check for existing appointments in the selected time window
        firestore.collection("availableDates")
            .whereEqualTo("doctorId", doctorId)
            .whereGreaterThanOrEqualTo("datetime", startTimeWindow)
            .whereLessThanOrEqualTo("datetime", endTimeWindow)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Inform user if appointment slot is already booked
                    Toast.makeText(this@SetAppointmentDocActivity, "Appointment slot is already booked", Toast.LENGTH_SHORT).show()
                } else {
                    // No overlapping appointment found, proceed to book
                    val timestamp = startTime

                    val appointment = AvailableDates(
                        availableDateId = "",
                        doctorId = doctorId,
                        datetime = timestamp,
                        locationId = selectedLocationId!!,
                        isAvailable = true
                    )

                    // Use coroutine scope to add appointment to Firestore
                    lifecycleScope.launch {
                        firestore.collection("availableDates").add(appointment)
                            .addOnSuccessListener { documentReference ->
                                val generatedReportId = documentReference.id

                                val updatedAppointment = appointment.copy(availableDateId = generatedReportId)

                                // Update appointment with generated ID
                                documentReference.set(updatedAppointment)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@SetAppointmentDocActivity, "Appointment set successfully", Toast.LENGTH_SHORT).show()
                                        // Clear input fields after successful booking
                                        editTextDate.setText("")
                                        autoLoc.setText("")
                                        selectedLocationId = null
                                        selectedDate = null
                                        selectedTime = null
                                    }
                                    .addOnFailureListener { exception ->
                                        // Handle errors updating appointment
                                        exception.printStackTrace()
                                        Toast.makeText(this@SetAppointmentDocActivity, "Failed to set appointment", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { exception ->
                                // Handle errors adding appointment
                                exception.printStackTrace()
                                Toast.makeText(this@SetAppointmentDocActivity, "Failed to set appointment", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors checking appointment slot
                exception.printStackTrace()
                Toast.makeText(this@SetAppointmentDocActivity, "Failed to check appointment slot", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Sets up an AutoCompleteTextView for location selection.
     * @param autoCompleteTextView The AutoCompleteTextView instance to set up.
     */
    private fun setupAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView) {
        autoCompleteTextView.inputType = 0
        autoCompleteTextView.isFocusable = false
        autoCompleteTextView.isClickable = true
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }
    }
}