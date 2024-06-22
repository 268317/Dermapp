package com.example.dermapp

import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MakeAppointmentPatActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var autoDateTime: AutoCompleteTextView
    private lateinit var autoDoc: AutoCompleteTextView
    private lateinit var autoLoc: AutoCompleteTextView
    private lateinit var bookButton: Button

    private var selectedDateTimeId: String? = null
    private var selectedDoctorId: String? = null
    private var selectedLocationId: String? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_appointment_pat)

        autoDateTime = findViewById(R.id.autoCompleteTextDate)
        autoDoc = findViewById(R.id.autoCompleteTextViewDoctor)
        autoLoc = findViewById(R.id.autoCompleteTextViewLocalization)
        bookButton = findViewById(R.id.bookButton)

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            startActivity(Intent(this, StartPatActivity::class.java))
        }

        autoDateTime.setOnClickListener {
            if (selectedDoctorId != null && selectedLocationId != null) {
                loadDoctorAvailableDatetime(selectedDoctorId!!, selectedLocationId!!)
            } else {
                Toast.makeText(this, "Please select a doctor and location first.", Toast.LENGTH_SHORT).show()
            }
        }

        setupAutoCompleteTextView(autoDoc)
        setupAutoCompleteTextView(autoLoc)

        loadDoctors()

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

    private fun loadDoctors() {
        val doctorsCollection = firestore.collection("doctors")
        doctorsCollection.get()
            .addOnSuccessListener { doctorsResult ->
                val doctorsList = doctorsResult.toObjects(Doctor::class.java)
                val doctorNames = doctorsList.map { "${it.firstName} ${it.lastName}" }.toTypedArray()
                val docAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, doctorNames)
                autoDoc.setAdapter(docAdapter)
                autoDoc.setOnItemClickListener { _, _, position, _ ->
                    selectedDoctorId = doctorsList[position].doctorId
                    loadDoctorLocations(selectedDoctorId!!)
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

                // Ustawienie formatera z odpowiednią strefą czasową
                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw") // Ustaw strefę czasową

                val dateTimes = availableDates.map { availableDate ->
                    // Formatowanie daty i godziny zgodnie z formaterem
                    dateFormat.format(availableDate.datetime)
                }.toTypedArray()

                if (dateTimes.isNotEmpty()) {
                    val dateTimeAdapter = ArrayAdapter(this@MakeAppointmentPatActivity, android.R.layout.simple_dropdown_item_1line, dateTimes)
                    autoDateTime.setAdapter(dateTimeAdapter)
                    autoDateTime.setOnItemClickListener { _, _, position, _ ->
                        selectedDateTimeId = availableDates[position].availableDateId
                    }
                    autoDateTime.showDropDown()
                } else {
                    Toast.makeText(this@MakeAppointmentPatActivity, "No available datetimes for the selected doctor and location.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MakeAppointmentPatActivity, "Failed to load available datetimes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bookAppointment() {
        val doctorId = selectedDoctorId ?: return
        val locationId = selectedLocationId ?: return
        val dateTimeId = selectedDateTimeId ?: return

        val doctor = autoDoc.text.toString()
        val location = autoLoc.text.toString()
        val dateTime = autoDateTime.text.toString()

        if (doctor.isNotEmpty() && location.isNotEmpty() && dateTime.isNotEmpty()) {
            try {
                // Utworzenie formatera z odpowiednią strefą czasową
                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw") // Ustaw strefę czasową

                // Parsowanie daty i godziny z formatu tekstowego
                val appointmentDate = dateFormat.parse(dateTime)

                val appointment = Appointment(
                    doctorId = doctorId,
                    patientPesel = "1234567890", // Placeholder for patient's PESEL, replace with actual logic to get patient data
                    appointmentDate = appointmentDate,
                    localization = location,
                    diagnosis = "", // Set diagnosis if applicable
                    recommendations = "" // Set recommendations if applicable
                )

                // Zapis wizyty do Firestore z automatycznie generowanym appointmentId
                firestore.collection("appointment")
                    .add(appointment)
                    .addOnSuccessListener { documentReference ->
                        val generatedAppointmentId = documentReference.id

                        // Aktualizacja appointmentId z wygenerowanym ID
                        val updatedAppointment = appointment.copy(appointmentId = generatedAppointmentId)

                        // Ustawienie appointmentId w dokumencie Firestore
                        documentReference.set(updatedAppointment)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Appointment booked successfully.", Toast.LENGTH_SHORT).show()

                                // Aktualizacja pola isAvailable w kolekcji availableDates
                                val availableDatesRef = firestore.collection("availableDates").document(dateTimeId)
                                availableDatesRef
                                    .update("isAvailable", false)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Availability updated successfully.", Toast.LENGTH_SHORT).show()

                                        // Wyczyszczenie pól po udanym zapisaniu wizyty
                                        autoDoc.setText("")
                                        autoLoc.setText("")
                                        autoDateTime.setText("")
                                        selectedDoctorId = null
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
        // Zatrzymanie wszystkich korutyn w zakresie, aby uniknąć wycieków pamięci
        coroutineScope.cancel()
    }
}
