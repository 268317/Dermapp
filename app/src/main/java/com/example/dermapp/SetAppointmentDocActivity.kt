package com.example.dermapp

import android.app.AlertDialog
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dermapp.database.AvailableDates
import com.example.dermapp.database.Doctor
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Activity for doctors to set appointments with patients.
 * This class enables doctors to select available dates, times, and locations for patient appointments.
 */
class SetAppointmentDocActivity : AppCompatActivity() {

    // UI components for setting up appointments
    private lateinit var backButton: ImageButton
    private lateinit var firestore: FirebaseFirestore
    private lateinit var editTextDate: EditText
    private lateinit var autoLoc: AutoCompleteTextView
    private lateinit var bookButton: Button
    private var selectedLocationId: String? = null
    private var selectedDate: Date? = null
    private var selectedTime: String? = null

    /**
     * Initializes UI components and sets up event listeners.
     * Fetches current doctor details and their available locations.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_appointment_doc)

        // Set the default time zone for the activity
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        // Initialize Firestore instance and UI elements
        firestore = FirebaseFirestore.getInstance()
        editTextDate = findViewById(R.id.editTextDate)
        autoLoc = findViewById(R.id.autoCompleteTextViewLocalization)
        bookButton = findViewById(R.id.bookButton)

        // Retrieve the current user's UID
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Fetch the doctor's details based on their UID
        val userRef = firestore.collection("doctors").document(currentUserUid!!)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(Doctor::class.java)

                // Load the doctor's available locations
                user?.let {
                    loadDoctorLocations(user.doctorId)
                    val doctorId = user.doctorId

                    // Set up booking button to book appointments
                    bookButton.setOnClickListener {
                        bookAppointment(doctorId)
                    }
                }
            }
        }.addOnFailureListener {
            // Handle errors while fetching doctor data from Firestore
        }

        // Set up autocomplete for selecting locations
        setupAutoCompleteTextView(autoLoc)

        // Show date picker when the date input field is clicked
        editTextDate.setOnClickListener {
            showDateTimePicker()
        }

        // Set up back button to navigate to the previous activity
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Loads the doctor's associated locations from Firestore.
     * @param doctorId The ID of the doctor whose locations need to be fetched.
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
            .addOnFailureListener {
                // Handle failures while loading locations
            }
    }

    /**
     * Displays a date and time picker for selecting the appointment's date and time.
     * Stores the selected values in the respective variables.
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

                // Format the selected date and time
                val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                dateTimeFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw")
                editTextDate.setText(dateTimeFormat.format(selectedDate.time))

                // Store the selected date and time
                this.selectedDate = selectedDate.time
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true)

            // Adjust time picker for today's date
            if (isToday) {
                timePicker.updateTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
            }

            timePicker.show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))

        // Set minimum date to today
        datePicker.datePicker.minDate = now.timeInMillis
        datePicker.show()
    }

    /**
     * Handles the booking process for an appointment.
     * Checks if the selected time slot is available and saves it to Firestore.
     * @param doctorId The ID of the doctor booking the appointment.
     */
    private fun bookAppointment(doctorId: String) {
        if (selectedLocationId == null || selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate the appointment's time window for overlap checking
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        val timeParts = selectedTime!!.split(":")
        calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
        calendar.set(Calendar.MINUTE, timeParts[1].toInt())

        val startTime = calendar.time
        calendar.add(Calendar.MINUTE, -9)
        val startTimeWindow = calendar.time
        calendar.add(Calendar.MINUTE, 18)
        val endTimeWindow = calendar.time

        // Query Firestore to check for overlapping appointments
        firestore.collection("availableDates")
            .whereEqualTo("doctorId", doctorId)
            .whereGreaterThanOrEqualTo("datetime", startTimeWindow)
            .whereLessThanOrEqualTo("datetime", endTimeWindow)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    Toast.makeText(this, "Appointment slot is already booked", Toast.LENGTH_SHORT).show()
                } else {
                    val appointment = AvailableDates(
                        availableDateId = "",
                        doctorId = doctorId,
                        datetime = startTime,
                        locationId = selectedLocationId!!,
                        isAvailable = true
                    )

                    // Add appointment to Firestore
                    lifecycleScope.launch {
                        firestore.collection("availableDates").add(appointment)
                            .addOnSuccessListener { documentReference ->
                                val updatedAppointment = appointment.copy(availableDateId = documentReference.id)

                                documentReference.set(updatedAppointment)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@SetAppointmentDocActivity, "Appointment set successfully", Toast.LENGTH_SHORT).show()
                                        editTextDate.setText("")
                                        autoLoc.setText("")
                                        selectedLocationId = null
                                        selectedDate = null
                                        selectedTime = null
                                        showBookingPrompt()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@SetAppointmentDocActivity, "Failed to set appointment", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@SetAppointmentDocActivity, "Failed to set appointment", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to check appointment slot", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Sets up the AutoCompleteTextView for selecting locations.
     * @param autoCompleteTextView The AutoCompleteTextView instance to configure.
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
     * Displays a prompt asking the user if they want to book another appointment.
     * Redirects to the main doctor activity if the user chooses not to book another.
     */
    private fun showBookingPrompt() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to book another appointment?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this, StartDocActivity::class.java))
                finish()
            }

        val alert = builder.create()
        alert.show()
    }
}
