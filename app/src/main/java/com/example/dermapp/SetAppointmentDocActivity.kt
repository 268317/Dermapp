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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_appointment_doc)

        firestore = FirebaseFirestore.getInstance()
        editTextDate = findViewById(R.id.editTextDate)
        autoLoc = findViewById(R.id.autoCompleteTextViewLocalization)
        bookButton = findViewById(R.id.bookButton)

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        val userRef = FirebaseFirestore.getInstance().collection("doctors").document(currentUserUid!!)

        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(Doctor::class.java)

                // Sprawdź, czy udało się pobrać dane użytkownika
                user?.let {
                    loadDoctorLocations(user.doctorId)
                    val doctorId = user.doctorId

                    bookButton.setOnClickListener {
                        bookAppointment(doctorId)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            // Obsłuż błędy pobierania danych z Firestore
        }

        setupAutoCompleteTextView(autoLoc)

        editTextDate.setOnClickListener {
            showDateTimePicker()
        }

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }
    }

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
                exception.printStackTrace()
            }
    }

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

                // Correctly store the selected date and time in local time
                this.selectedDate = selectedDate.time
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true)

            if (isToday) {
                timePicker.updateTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
            }

            timePicker.show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))

        datePicker.datePicker.minDate = now.timeInMillis
        datePicker.show()
    }
    private fun bookAppointment(doctorId: String) {
        if (selectedLocationId == null || selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate the start and end times for the window around the selected time
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

        firestore.collection("availableDates")
            .whereEqualTo("doctorId", doctorId)
            .whereGreaterThanOrEqualTo("datetime", startTimeWindow)
            .whereLessThanOrEqualTo("datetime", endTimeWindow)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
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

                    lifecycleScope.launch {
                        firestore.collection("availableDates").add(appointment)
                            .addOnSuccessListener { documentReference ->
                                val generatedReportId = documentReference.id

                                val updatedAppointment = appointment.copy(availableDateId = generatedReportId)

                                documentReference.set(updatedAppointment)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@SetAppointmentDocActivity, "Appointment set successfully", Toast.LENGTH_SHORT).show()
                                        editTextDate.setText("")
                                        autoLoc.setText("")
                                        selectedLocationId = null
                                        selectedDate = null
                                        selectedTime = null
                                    }
                                    .addOnFailureListener { exception ->
                                        exception.printStackTrace()
                                        Toast.makeText(this@SetAppointmentDocActivity, "Failed to set appointment", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { exception ->
                                exception.printStackTrace()
                                Toast.makeText(this@SetAppointmentDocActivity, "Failed to set appointment", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Toast.makeText(this@SetAppointmentDocActivity, "Failed to check appointment slot", Toast.LENGTH_SHORT).show()
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
}