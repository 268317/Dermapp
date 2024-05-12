package com.example.dermapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.TextView


class CreateAppointmentDetailsDocActivity : AppCompatActivity() {

    // Declare UI elements
    private lateinit var textViewAppointmentDateDoc: TextView
    private lateinit var textViewDateAppointmentDoc: TextView
    private lateinit var textViewPatientAppointmentDoc: TextView
    private lateinit var textViewFirstNameAppointmentDoc: TextView
    private lateinit var textViewLastNameAppointmentDoc: TextView
    private lateinit var textViewPeselAppointmentDoc: TextView
    private lateinit var textViewDiagnosisAppointmentDoc: TextView
    private lateinit var editTextMultiLineDiagnosisAppointmentDoc: EditText
    private lateinit var textViewRecommendationsAppointmentDoc: TextView
    private lateinit var editTextMultiLineRecommendationsAppointmentDoc: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_appointment_details_doc)

        // Initialize UI elements
        textViewAppointmentDateDoc = findViewById(R.id.textViewAppointmentDateDoc)
        textViewDateAppointmentDoc = findViewById(R.id.textViewDateAppointmentDoc)
        textViewPatientAppointmentDoc = findViewById(R.id.textViewPatientAppointmentDoc)
        textViewFirstNameAppointmentDoc = findViewById(R.id.textViewFirstNameAppointmentDoc)
        textViewLastNameAppointmentDoc = findViewById(R.id.textViewLastNameAppointmentDoc)
        textViewPeselAppointmentDoc = findViewById(R.id.textViewPeselAppointmentDoc)
        editTextMultiLineDiagnosisAppointmentDoc = findViewById(R.id.editTextTextMultiLineDiagnosisAppointmentDoc)
        editTextMultiLineRecommendationsAppointmentDoc = findViewById(R.id.editTextTextMultiLineRecommendationsAppointmentDoc)

    }
}
