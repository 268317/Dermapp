package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AppointmentDetailsPatActivity : AppCompatActivity() {

    // Declare UI elements
    private lateinit var appointmentDate: TextView
    private lateinit var appointmentDocFirstName: TextView
    private lateinit var appointmentDocLastName: TextView
    private lateinit var appointmentDocId: TextView
    private lateinit var appointmentLoc: TextView
    private lateinit var backButton: ImageButton
    private lateinit var textRecommendations: TextView
    private lateinit var textDiagnosis: TextView


    private val firestore = FirebaseFirestore.getInstance()
    // SimpleDateFormat configured for date and time in Warsaw timezone
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_details_pat)

        // Initialize UI elements with error checking
        appointmentDate = findViewById(R.id.textViewDateAppointmentPat)
        appointmentDocFirstName = findViewById(R.id.textViewDoctorNameAppointmentPat)
        appointmentDocLastName = findViewById(R.id.textViewDocLastNameAppointmentPat)
        appointmentDocId = findViewById(R.id.textViewDoctorIDAppointmentPat)
        appointmentLoc = findViewById(R.id.textViewAppointmentLocEnter)
        textRecommendations = findViewById(R.id.editTextMultiLineRecommendationsAppointmentPat)
        textDiagnosis = findViewById(R.id.editTextMultiLineDiagnosisAppointmentPat)

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
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
                    val doctorId = appointmentDocument.getString("doctorId") ?: ""
                    val datetime = appointmentDocument.getDate("datetime")
                    val localization = appointmentDocument.getString("localization") ?: ""
                    val recommendation = appointmentDocument.getString("recommendations") ?:""
                    val diagnosis = appointmentDocument.getString("diagnosis") ?:""

                    appointmentDate.text = if (datetime != null) dateTimeFormatter.format(datetime) else "Unknown"
                    appointmentLoc.text = localization
                    textRecommendations.text = recommendation
                    textDiagnosis.text = diagnosis

                    appointmentDocId.text = doctorId
                    // Fetch doctor details
                    val querySnapshot = firestore.collection("doctors")
                        .whereEqualTo("doctorId", doctorId)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val doctorDocument = querySnapshot.documents[0] // Assuming there's only one matching document
                        val firstName = doctorDocument.getString("firstName") ?: ""
                        val lastName = doctorDocument.getString("lastName") ?: ""
                        appointmentDocFirstName.text = firstName
                        appointmentDocLastName.text = lastName
                    } else {
                        appointmentDocFirstName.text = "Unknown"
                        appointmentDocLastName.text = "Doctor"
                    }

                } else {
                    appointmentDate.text = "Unknown"
                    appointmentDocId.text = "doctorId"
                    appointmentLoc.text = "localization"
                    appointmentDocFirstName.text = "Unknown"
                    appointmentDocLastName.text = "Doctor"
                }
            } catch (e: Exception) {
                Log.e("AppointmentDetailsPat", "Error fetching appointment details", e)
                appointmentDate.text = "Unknown"
                appointmentDocId.text = "doctorId"
                appointmentLoc.text = "localization"
                appointmentDocFirstName.text = "Unknown"
                appointmentDocLastName.text = "Doctor"
            }
        }
    }
}
