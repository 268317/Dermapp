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

/**
 * Activity to display the details of an appointment for patients.
 * Fetches and displays data such as doctor details, appointment location,
 * date, diagnosis, and recommendations from the Firebase Firestore database.
 */
class AppointmentDetailsPatActivity : AppCompatActivity() {

    // Declare UI elements for displaying appointment details
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

    // Instance of Firebase Firestore used for database operations
    private val firestore = FirebaseFirestore.getInstance()

    // Formatter for date and time, configured for Warsaw timezone
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    }

    /**
     * Called when the activity is starting.
     * Sets up the UI, initializes listeners, and retrieves appointment data from Firestore.
     *
     * @param savedInstanceState Contains the data it most recently supplied in `onSaveInstanceState`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_details_pat)

        // Set the timezone to Warsaw for this activity
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        // Initialize UI elements by linking them with their IDs in the layout
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

        // Set a click listener on the back button to navigate back to the start screen
        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        // Retrieve the UID of the currently logged-in user
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Get the appointment ID passed through the Intent
        val appointmentId = intent.getStringExtra("appointmentId")

        // Fetch appointment details from Firestore using a coroutine
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                // Retrieve the appointment document from Firestore
                val appointmentDocument = firestore.collection("appointment")
                    .document(appointmentId!!)
                    .get()
                    .await()

                if (appointmentDocument.exists()) {
                    // Extract details from the appointment document
                    val doctorId = appointmentDocument.getString("doctorId") ?: ""
                    val datetime = appointmentDocument.getDate("datetime")
                    val localization = appointmentDocument.getString("localization") ?: ""
                    val recommendation = appointmentDocument.getString("recommendations") ?: ""
                    val diagnosis = appointmentDocument.getString("diagnosis") ?: ""

                    // Format and display the appointment date and time
                    appointmentDate.text = if (datetime != null) dateTimeFormatter.format(datetime) else "Unknown"
                    textRecommendations.text = recommendation
                    textDiagnosis.text = diagnosis
                    appointmentDocId.text = doctorId

                    // Split the localization string into address parts for display
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

                    // Fetch and display doctor details based on doctor ID
                    val querySnapshot = firestore.collection("doctors")
                        .whereEqualTo("doctorId", doctorId)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val doctorDocument = querySnapshot.documents[0] // Assuming one matching document
                        val firstName = doctorDocument.getString("firstName") ?: ""
                        val lastName = doctorDocument.getString("lastName") ?: ""
                        appointmentDocFirstName.text = firstName
                        appointmentDocLastName.text = lastName
                    } else {
                        appointmentDocFirstName.text = "Unknown"
                        appointmentDocLastName.text = "Doctor"
                    }
                } else {
                    // Set default values if the document does not exist
                    appointmentDate.text = "Unknown"
                    appointmentDocId.text = "doctorId"
                    appointmentLoc.text = "localization"
                    appointmentDocFirstName.text = "Unknown"
                    appointmentDocLastName.text = "Doctor"
                }
            } catch (e: Exception) {
                // Log and handle errors during Firestore operations
                Log.e("AppointmentDetailsPat", "Error fetching appointment details", e)
                appointmentDate.text = "Unknown"
                appointmentDocId.text = "doctorId"
                appointmentLoc.text = "localization"
                appointmentDocFirstName.text = "Unknown"
                appointmentDocLastName.text = "Doctor"
            }
        }

        // Initialize the button for displaying the location on a map
        buttonShowOnMap = findViewById(R.id.buttonShowOnMap)

        // Set a click listener to navigate to the MapsActivity with the address details
        buttonShowOnMap.setOnClickListener {
            val address = appointmentLoc.text.toString() + " " + appointmentLoc2.text.toString()
            Log.e("ADDRESS", address)

            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("address", address)
            startActivity(intent)
        }
    }
}
