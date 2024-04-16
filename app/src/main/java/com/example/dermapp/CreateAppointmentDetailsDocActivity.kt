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
        textViewDiagnosisAppointmentDoc = findViewById(R.id.textViewDiagnosisAppointmentDoc)
        editTextMultiLineDiagnosisAppointmentDoc = findViewById(R.id.editTextTextMultiLineDiagnosisAppointmentDoc)
        textViewRecommendationsAppointmentDoc = findViewById(R.id.textViewRecommendationsAppointmentDoc)
        editTextMultiLineRecommendationsAppointmentDoc = findViewById(R.id.editTextTextMultiLineRecommendationsAppointmentDoc)

//        // Set example data to the TextViews (replace with actual data)
//        textViewAppointmentDateDoc.text = "Appointment date:"
//        textViewDateAppointmentDoc.text = "DD MONTH YYYY, 00:00"
//        textViewPatientAppointmentDoc.text = "Patient:"
//        textViewFirstNameAppointmentDoc.text = "First name:"
//        textViewLastNameAppointmentDoc.text = "Last name:"
//        textViewPeselAppointmentDoc.text = "PESEL:"
//        textViewDiagnosisAppointmentDoc.text = "Diagnosis"
//        textViewRecommendationsAppointmentDoc.text = "Recommendations"
//
//        // Set example text to the EditTexts (replace with actual text)
//        editTextMultiLineDiagnosisAppointmentDoc.setText("Diagnosis details go here...")
//        editTextMultiLineRecommendationsAppointmentDoc.setText("Recommendation details go here...")
    }
}
