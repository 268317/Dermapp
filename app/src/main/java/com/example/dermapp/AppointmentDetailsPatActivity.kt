package com.example.dermapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.database.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppointmentDetailsPatActivity : AppCompatActivity() {

    // Declare UI elements
    private lateinit var textViewAppointmentDatePat: TextView
    private lateinit var textViewDateAppointmentPat: TextView
    private lateinit var textViewDoctorAppointmentPat: TextView
    private lateinit var textViewFirstNameAppointmentPat: TextView
    private lateinit var textViewLastNameAppointmentPat: TextView
    private lateinit var textViewDocIDAppointmentPat: TextView
    private lateinit var textViewDiagnosisAppointmentPat: TextView
    private lateinit var textViewMultiDiagnosisAppointmentPat: TextView
    private lateinit var textViewRecommendationAppointmentPat: TextView
    private lateinit var textViewMultiRecommendationAppointmentPat: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_details_pat)

        // Initialize UI elements
        textViewAppointmentDatePat = findViewById(R.id.textViewAppointmentDatePat)
        textViewDateAppointmentPat = findViewById(R.id.textViewDateAppointmentPat)
        textViewDoctorAppointmentPat = findViewById(R.id.textViewDoctorAppointmentPat)
        textViewFirstNameAppointmentPat = findViewById(R.id.textViewFirstNameAppointmentPat)
        textViewLastNameAppointmentPat = findViewById(R.id.textViewLastNameAppointmentPat)
        textViewDocIDAppointmentPat = findViewById(R.id.textViewDocIDAppointmentPat)
        textViewDiagnosisAppointmentPat = findViewById(R.id.textViewDiagnosisAppointmentPat)
        textViewMultiDiagnosisAppointmentPat = findViewById(R.id.textViewMultiDiagnosisAppointmentPat)
        textViewRecommendationAppointmentPat = findViewById(R.id.textViewRecommendationAppointmentPat)
        textViewMultiRecommendationAppointmentPat = findViewById(R.id.textViewMultiRecommendationAppointmentPat)


        // Set example data to the TextViews (replace with actual data)
        textViewAppointmentDatePat.text = "Appointment date:"
        textViewDateAppointmentPat.text = "DD MONTH YYYY, 00:00"
        textViewDoctorAppointmentPat.text = "Doctor:"
        textViewFirstNameAppointmentPat.text = "First name:"
        textViewLastNameAppointmentPat.text = "Last name:"
        textViewDocIDAppointmentPat.text = "Doctor ID:"
        textViewDiagnosisAppointmentPat.text = "Diagnosis"
        textViewMultiDiagnosisAppointmentPat.text = "Diagnosis details go here..."
        textViewRecommendationAppointmentPat.text = "Recommendations"
        textViewMultiRecommendationAppointmentPat.text = "Recommendation details go here..."


        // Pobierz UID aktualnie zalogowanego użytkownika
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Utwórz odwołanie do dokumentu użytkownika w Firestore
        val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserUid!!)

        // Pobierz dane użytkownika z Firestore
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Konwertuj dane na obiekt użytkownika
                val user = documentSnapshot.toObject(AppUser::class.java)

                // Sprawdź, czy udało się pobrać dane użytkownika
                user?.let {
                    // Ustaw imię użytkownika w nagłówku
                    val headerNameTextView: TextView = findViewById(R.id.firstNameTextView)
                    headerNameTextView.text = user.firstName
                }
            }
        }.addOnFailureListener { exception ->
            // Obsłuż błędy pobierania danych z Firestore
        }

    }
}
