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
import com.example.dermapp.database.Doctor
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class MakeAppointmentPatActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var textDate: AutoCompleteTextView
    private lateinit var autoDoc: AutoCompleteTextView
    private lateinit var autoLoc: AutoCompleteTextView
    private lateinit var bookButton: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_make_appointment_pat)

        textDate = findViewById(R.id.autoCompleteTextDate)
        autoDoc = findViewById(R.id.autoCompleteTextViewDoctor)
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

        val doctorsCollection = FirebaseFirestore.getInstance().collection("doctors")
        doctorsCollection.get().addOnSuccessListener { doctorsResult ->
            val doctorsList = doctorsResult.toObjects(Doctor::class.java)
            val doctorNames = doctorsList.map { "${it.lastName} ${it.firstName}" }.toTypedArray()
            val docAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, doctorNames)
            autoDoc.setAdapter(docAdapter)
        }
        //val docOptions = arrayOf("Doc 1", "Doc 2", "Doc 3", "Doc 4") // Chwilowe opcje doc
        val locOptions = arrayOf("Loc 1", "Loc 2", "Loc 3", "Loc 4") // Chwilowe opcje loc

        //val docAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, docOptions)
        //autoDoc.setAdapter(docAdapter)

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
        val doctor = autoDoc.text.toString()
        val location = autoLoc.text.toString()
        val date = textDate.text.toString()

    }

}