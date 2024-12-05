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
import java.util.TimeZone

/**
 * Activity for displaying and editing appointment details for doctors.
 * Enables doctors to view, edit, and update details such as diagnosis and recommendations for an appointment.
 */
class CreateAppointmentDetailsDocActivity : AppCompatActivity() {

    // Declare UI elements for the activity
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

    // Instance of Firestore for database operations
    private val firestore = FirebaseFirestore.getInstance()

    // Appointment ID passed from the intent
    private var appointmentId: String? = null

    /**
     * Called when the activity is starting.
     * Sets up the UI, initializes listeners, and fetches appointment details from Firestore.
     *
     * @param savedInstanceState Contains the data it most recently supplied in `onSaveInstanceState`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_appointment_details_doc)

        // Set the timezone for consistent time handling
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        // Initialize UI elements by linking them to their respective IDs
        textViewAppointmentDateDoc = findViewById(R.id.textViewAppointmentDateDoc)
        textViewDateAppointmentDoc = findViewById(R.id.textViewDateAppointmentDoc)
        textViewPatientAppointmentDoc = findViewById(R.id.textViewPatientAppointmentDoc)
        textViewFirstNameAppointmentDoc = findViewById(R.id.textViewPatientFirstNameAppointmentDoc)
        textViewLastNameAppointmentDoc = findViewById(R.id.textViewPatientLastNameAppointmentDoc)
        textViewPeselAppointmentDoc = findViewById(R.id.textViewPatientPeselAppointmentDoc)
        editTextMultiLineDiagnosisAppointmentDoc = findViewById(R.id.editTextTextMultiLineDiagnosisAppointmentDoc)
        editTextMultiLineRecommendationsAppointmentDoc = findViewById(R.id.editTextTextMultiLineRecommendationsAppointmentDoc)
        editButton = findViewById(R.id.bookButton)

        // Initialize the back button and set a click listener for navigation
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }

        // Retrieve the appointment ID from the Intent
        appointmentId = intent.getStringExtra("appointmentId")

        // Fetch and display appointment details using a coroutine
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

                    // Format the date for display
                    val formattedDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                        .format(datetime)

                    // Update UI elements with the fetched appointment details
                    textViewDateAppointmentDoc.text = formattedDate
                    editTextMultiLineDiagnosisAppointmentDoc.setText(diagnosis)
                    editTextMultiLineRecommendationsAppointmentDoc.setText(recommendations)

                    // Fetch and display patient details
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
                        // Handle the case where patient details are missing
                        textViewFirstNameAppointmentDoc.text = "Unknown"
                        textViewLastNameAppointmentDoc.text = "Patient"
                        textViewPeselAppointmentDoc.text = "Unknown"
                    }
                } else {
                    // Handle the case where appointment details are missing
                    textViewDateAppointmentDoc.text = "Unknown"
                    textViewFirstNameAppointmentDoc.text = "Unknown"
                    textViewLastNameAppointmentDoc.text = "Patient"
                    textViewPeselAppointmentDoc.text = "Unknown"
                }
            } catch (e: Exception) {
                // Handle any exceptions during data fetching
                textViewDateAppointmentDoc.text = "Unknown"
                textViewFirstNameAppointmentDoc.text = "Unknown"
                textViewLastNameAppointmentDoc.text = "Patient"
                textViewPeselAppointmentDoc.text = "Unknown"
            }
        }

        // Set a click listener for the edit button to update appointment details
        editButton.setOnClickListener {
            val diagnosis = editTextMultiLineDiagnosisAppointmentDoc.text.toString()
            val recommendations = editTextMultiLineRecommendationsAppointmentDoc.text.toString()

            // Update Firestore document with the edited diagnosis and recommendations
            appointmentId?.let { appointmentId ->
                firestore.collection("appointment")
                    .document(appointmentId)
                    .update(mapOf(
                        "diagnosis" to diagnosis,
                        "recommendations" to recommendations
                    ))
                    .addOnSuccessListener {
                        // Navigate back to the appointment details activity after successful update
                        val intent = Intent(this, AppointmentDetailsDocActivity::class.java)
                        intent.putExtra("appointmentId", appointmentId)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        // Handle any errors during the update process
                    }
            }
        }
    }
}
