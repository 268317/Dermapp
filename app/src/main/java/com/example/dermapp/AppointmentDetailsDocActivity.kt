package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dermapp.startDoctor.StartDocActivity
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Activity to display the details of an appointment for doctors.
 */
class AppointmentDetailsDocActivity : AppCompatActivity() {

    // Declare UI elements
    private lateinit var appointmentDate: TextView
    private lateinit var appointmentPatFirstName: TextView
    private lateinit var appointmentPatLastName: TextView
    private lateinit var appointmentPatPesel: TextView
    private lateinit var appointmentLoc: TextView
    private lateinit var backButton: ImageButton
    private lateinit var textRecommendations: TextView
    private lateinit var textDiagnosis: TextView

    // Instance of Firebase Firestore
    private val firestore = FirebaseFirestore.getInstance()

    // SimpleDateFormat configured for date and time in Warsaw timezone
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    }

    /**
     * Called when the activity is starting.
     * Sets up UI elements, initializes listeners, and retrieves necessary data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_details_doc)

        // Ustawienie strefy czasowej dla aktywności
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        // Initialize UI elements with error checking
        appointmentDate = findViewById(R.id.textViewDateAppointmentDoc)
        appointmentPatFirstName = findViewById(R.id.textViewPatNameAppointmentDoc)
        appointmentPatLastName = findViewById(R.id.textViewPatLastNameAppointmentDoc)
        appointmentPatPesel = findViewById(R.id.textViewPatPeselAppointmentDoc)
        appointmentLoc = findViewById(R.id.textViewAppointmentLocEnter)

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }

        // Get UID of the currently logged in user
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Get appointmentId from Intent
        val appointmentId = intent.getStringExtra("appointmentId")

        // Fetch appointment details using coroutine
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val appointmentDocument = firestore.collection("appointment")
                    .document(appointmentId!!)
                    .get()
                    .await()

                if (appointmentDocument.exists()) {
                    val patientId = appointmentDocument.getString("patientId") ?: ""
                    val datetime = appointmentDocument.getDate("datetime")
                    val localization = appointmentDocument.getString("localization") ?: ""
                    val recommendation = appointmentDocument.getString("recommendations") ?: ""
                    val diagnosis = appointmentDocument.getString("diagnosis") ?: ""

                    appointmentDate.text = if (datetime != null) dateTimeFormatter.format(datetime) else "Unknown"
                    appointmentLoc.text = localization
                    textRecommendations.text = recommendation
                    textDiagnosis.text = diagnosis

                    // Fetch patient details
                    val querySnapshot = firestore.collection("patients")
                        .whereEqualTo("userId", patientId)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val doctorDocument = querySnapshot.documents[0] // Assuming there's only one matching document
                        val firstName = doctorDocument.getString("firstName") ?: ""
                        val lastName = doctorDocument.getString("lastName") ?: ""
                        val pesel = doctorDocument.getString("pesel")
                        appointmentPatFirstName.text = firstName
                        appointmentPatLastName.text = lastName
                        appointmentPatPesel.text = pesel
                    } else {
                        appointmentPatFirstName.text = "Unknown"
                        appointmentPatLastName.text = "Patient"
                        appointmentPatPesel.text = "patient pesel"
                    }

                } else {
                    appointmentDate.text = "Unknown"
                    appointmentPatPesel.text = "patient pesel"
                    appointmentLoc.text = "localization"
                    appointmentPatFirstName.text = "Unknown"
                    appointmentPatLastName.text = "Patient"
                }
            } catch (e: Exception) {
                Log.e("AppointmentDetailsPat", "Error fetching appointment details", e)
                appointmentDate.text = "Unknown"
                appointmentPatPesel.text = "patient pesel"
                appointmentLoc.text = "localization"
                appointmentPatFirstName.text = "Unknown"
                appointmentPatLastName.text = "Patient"
            }
        }
    }
}