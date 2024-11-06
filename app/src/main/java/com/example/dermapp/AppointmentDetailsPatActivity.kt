package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
import kotlin.math.log


/**
 * Activity to display the details of an appointment for patients.
 */
class AppointmentDetailsPatActivity : AppCompatActivity() {

    // Declare UI elements
    private lateinit var appointmentDate: TextView
    private lateinit var appointmentDocFirstName: TextView
    private lateinit var appointmentDocLastName: TextView
    private lateinit var appointmentDocId: TextView
    private lateinit var appointmentLoc: TextView
    private lateinit var appointmentLoc2: TextView
    private lateinit var backButton: ImageButton
    private lateinit var textRecommendations: TextView
    private lateinit var textDiagnosis: TextView
    private lateinit var buttonShowOnMap: Button

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
        setContentView(R.layout.activity_appointment_details_pat)

        // Ustawienie strefy czasowej dla aktywności
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        // Initialize UI elements with error checking
        appointmentDate = findViewById(R.id.textViewDateAppointmentPat)
        appointmentDocFirstName = findViewById(R.id.textViewDoctorNameAppointmentPat)
        appointmentDocLastName = findViewById(R.id.textViewDocLastNameAppointmentPat)
        appointmentDocId = findViewById(R.id.textViewDoctorIDAppointmentPat)
        appointmentLoc = findViewById(R.id.textViewAppointmentLocEnter)
        appointmentLoc2 = findViewById(R.id.textViewAppointmentLoc2Enter)
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
                    val recommendation = appointmentDocument.getString("recommendations") ?: ""
                    val diagnosis = appointmentDocument.getString("diagnosis") ?: ""

                    appointmentDate.text = if (datetime != null) dateTimeFormatter.format(datetime) else "Unknown"
                    textRecommendations.text = recommendation
                    textDiagnosis.text = diagnosis

                    appointmentDocId.text = doctorId
                    // Fetch doctor details

                    val addressParts = localization.split(",")
                    if (addressParts.size >= 2) {
                        val streetAndNumber = addressParts[0].trim()
                        val postalAndCity = addressParts[1].trim()

                        appointmentLoc.text = "$streetAndNumber,"
                        appointmentLoc2.text = postalAndCity
                    } else {
                        appointmentLoc.text = localization
                        appointmentLoc2.text = ""
                    }

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

        buttonShowOnMap = findViewById(R.id.buttonShowOnMap)

        buttonShowOnMap.setOnClickListener {
            val address = appointmentLoc.text.toString() + " " + appointmentLoc2.text.toString()

            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("address", address)
            startActivity(intent)
        }

    }
}