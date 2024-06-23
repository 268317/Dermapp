package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class CreateAppointmentDetailsDocActivity : AppCompatActivity() {

    // Declare UI elements
    private lateinit var textViewAppointmentDateDoc: TextView
    private lateinit var textViewDateAppointmentDoc: TextView
    private lateinit var textViewPatientAppointmentDoc: TextView
    private lateinit var textViewFirstNameAppointmentDoc: TextView
    private lateinit var textViewLastNameAppointmentDoc: TextView
    private lateinit var textViewPeselAppointmentDoc: TextView
    private lateinit var editTextMultiLineDiagnosisAppointmentDoc: EditText
    private lateinit var editTextMultiLineRecommendationsAppointmentDoc: EditText
    private lateinit var backButton: ImageButton
    private lateinit var editButton: Button
    private val firestore = FirebaseFirestore.getInstance()
    private var appointmentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_appointment_details_doc)

        // Initialize UI elements
        textViewAppointmentDateDoc = findViewById(R.id.textViewAppointmentDateDoc)
        textViewDateAppointmentDoc = findViewById(R.id.textViewDateAppointmentDoc)
        textViewPatientAppointmentDoc = findViewById(R.id.textViewPatientAppointmentDoc)
        textViewFirstNameAppointmentDoc = findViewById(R.id.textViewPatientFirstNameAppointmentDoc)
        textViewLastNameAppointmentDoc = findViewById(R.id.textViewPatientLastNameAppointmentDoc)
        textViewPeselAppointmentDoc = findViewById(R.id.textViewPatientPeselAppointmentDoc)
        editTextMultiLineDiagnosisAppointmentDoc = findViewById(R.id.editTextTextMultiLineDiagnosisAppointmentDoc)
        editTextMultiLineRecommendationsAppointmentDoc = findViewById(R.id.editTextTextMultiLineRecommendationsAppointmentDoc)
        editButton = findViewById(R.id.bookButton)

        // Initialize back button and its click listener
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }

        // Get appointmentId from Intent
        appointmentId = intent.getStringExtra("appointmentId")

        // Fetch appointment details using coroutine
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val appointmentDocument = firestore.collection("appointment")
                    .document(appointmentId!!)
                    .get()
                    .await()

                if (appointmentDocument.exists()) {
                    val patientId = appointmentDocument.getString("patientId") ?: ""
                    val datetime = appointmentDocument.getDate("datetime")
                    val diagnosis = appointmentDocument.getString("diagnosis") ?: ""
                    val recommendations = appointmentDocument.getString("recommendations") ?: ""

                    // Format date
                    val formattedDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                        .format(datetime)

                    textViewDateAppointmentDoc.text = formattedDate
                    editTextMultiLineDiagnosisAppointmentDoc.setText(diagnosis)
                    editTextMultiLineRecommendationsAppointmentDoc.setText(recommendations)

                    // Fetch patient details
                    val patientDocument = firestore.collection("patients")
                        .document(patientId)
                        .get()
                        .await()

                    if (patientDocument.exists()) {
                        val firstName = patientDocument.getString("firstName") ?: ""
                        val lastName = patientDocument.getString("lastName") ?: ""
                        val pesel = patientDocument.getString("pesel") ?: ""

                        textViewFirstNameAppointmentDoc.text = firstName
                        textViewLastNameAppointmentDoc.text = lastName
                        textViewPeselAppointmentDoc.text = pesel
                    } else {
                        textViewFirstNameAppointmentDoc.text = "Unknown"
                        textViewLastNameAppointmentDoc.text = "Patient"
                        textViewPeselAppointmentDoc.text = "patient pesel"
                    }

                } else {
                    textViewDateAppointmentDoc.text = "Unknown"
                    textViewFirstNameAppointmentDoc.text = "Unknown"
                    textViewLastNameAppointmentDoc.text = "Patient"
                    textViewPeselAppointmentDoc.text = "patient pesel"
                }
            } catch (e: Exception) {
                // Handle exceptions
                textViewDateAppointmentDoc.text = "Unknown"
                textViewFirstNameAppointmentDoc.text = "Unknown"
                textViewLastNameAppointmentDoc.text = "Patient"
                textViewPeselAppointmentDoc.text = "patient pesel"
            }
        }

        // Set click listener for editButton
        editButton.setOnClickListener {
            val diagnosis = editTextMultiLineDiagnosisAppointmentDoc.text.toString()
            val recommendations = editTextMultiLineRecommendationsAppointmentDoc.text.toString()

            // Update firestore document with new values
            appointmentId?.let { appointmentId ->
                firestore.collection("appointment")
                    .document(appointmentId)
                    .update(mapOf(
                        "diagnosis" to diagnosis,
                        "recommendations" to recommendations
                    ))
                    .addOnSuccessListener {
                        val intent = Intent(this, AppointmentDetailsDocActivity::class.java)
                        intent.putExtra("appointmentId", appointmentId)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                    }
            }
        }
    }
}