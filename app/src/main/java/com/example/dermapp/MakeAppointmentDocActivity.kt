package com.example.dermapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
<<<<<<< HEAD
import com.example.dermapp.database.Appointment
import com.example.dermapp.database.AvailableDates
import com.example.dermapp.database.Location
import com.example.dermapp.database.Patient
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
=======
import com.example.dermapp.startPatient.StartPatActivity
import java.util.Calendar
>>>>>>> parent of 14438d7 (RV update)

class MakeAppointmentDocActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var textDate: AutoCompleteTextView
    private lateinit var autoPat: AutoCompleteTextView
    private lateinit var autoLoc: AutoCompleteTextView
    private lateinit var bookButton: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_make_appointment_doc)

        textDate = findViewById(R.id.autoCompleteTextDate)
        autoPat = findViewById(R.id.autoCompleteTextViewPat)
        autoLoc = findViewById(R.id.autoCompleteTextViewLocalization)
        bookButton = findViewById(R.id.bookButton)

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        textDate.setOnClickListener{
            openCalendar()
        }

        val docOptions = arrayOf("Pat 1", "Pat 2", "Pat 3", "Pat 4") // Chwilowe opcje doc
        val locOptions = arrayOf("Loc 1", "Loc 2", "Loc 3", "Loc 4") // Chwilowe opcje loc

        val docAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, docOptions)
        autoPat.setAdapter(docAdapter)

        val locAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locOptions)
        autoLoc.setAdapter(locAdapter)

        bookButton.setOnClickListener{
            bookAppointment()
        }

    }

    private fun openCalendar() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDay = String.format("%02d", selectedDay)
            val formattedMonth = String.format("%02d", selectedMonth + 1)
            textDate.setText("$formattedDay-$formattedMonth-$selectedYear")
        }, year, month, dayOfMonth)

        datePickerDialog.show()
    }

    private fun bookAppointment() {
        val patient = autoPat.text.toString()
        val location = autoLoc.text.toString()
        val date = textDate.text.toString()

<<<<<<< HEAD
        if (doctor.isNotEmpty() && location.isNotEmpty() && dateTime.isNotEmpty()) {
            try {
                // Utworzenie formatera z odpowiednią strefą czasową
                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw") // Ustaw strefę czasową

                // Parsowanie daty i godziny z formatu tekstowego
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

                                val generatedAppointmentId = documentReference.id

                                // Aktualizacja pola isAvailable w kolekcji availableDates
                                val availableDatesRef = firestore.collection("availableDates").document(dateTimeId)
                                availableDatesRef
                                    .update("isAvailable", false)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Availability updated successfully.", Toast.LENGTH_SHORT).show()

                                        //Ustawienie powiadomienia
                                        setAppointmentReminder(generatedAppointmentId, appointmentTimeInMillis, location)

                                        // Wyczyszczenie pól po udanym zapisaniu wizyty
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
=======
>>>>>>> parent of 14438d7 (RV update)
    }

}