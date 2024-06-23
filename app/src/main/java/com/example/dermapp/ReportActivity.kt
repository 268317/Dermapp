package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
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

class ReportActivity : AppCompatActivity(){
    private lateinit var doctorName: TextView
    private lateinit var doctorFirstName: TextView
    private lateinit var doctorLastName: TextView
    private lateinit var checkedSymptoms: TextView
    private lateinit var textOther: TextView
    private lateinit var imageReport: ImageView
    private lateinit var backButton: Button

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_details_pat)

        // Initialize UI elements with error checking
        doctorName = findViewById(R.id.textViewDoctorName)
        checkedSymptoms = findViewById(R.id.textViewCheckedSymptoms)
        textOther = findViewById(R.id.textViewOther)
        imageReport = findViewById(R.id.imageReport)

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        // Get UID of the currently logged in user
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Get appointmentId from Intent
        val reportId = intent.getStringExtra("reportId")

        // Fetch appointment details using coroutine
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val reportDocument = firestore.collection("report")
                    .document(reportId!!)
                    .get()
                    .await()

                if (reportDocument.exists()) {
                    val doctorId = reportDocument.getString("doctorId") ?: ""
                    val photo = reportDocument.getString("attachmentUrl")
                    val other = reportDocument.getString("otherInfo") ?: ""

                    // Checking symptoms and adding them to checkedSymptoms
                    val symptoms = listOf(
                        "blackheads",
                        "discoloration",
                        "dryness",
                        "itching",
                        "moleChanges",
                        "newMole",
                        "pimples",
                        "rash",
                        "redness",
                        "seborrhoea",
                        "severeAcne",
                        "warts"
                    )

                    val symptomNames = listOf(
                        "Blackheads",
                        "Discoloration",
                        "Dryness",
                        "Itching",
                        "Mole Changes",
                        "New Mole",
                        "Pimples",
                        "Rash",
                        "Redness",
                        "Seborrhoea",
                        "Severe Acne",
                        "Warts"
                    )

                    val checkedSymptomList = mutableListOf<String>()
                    for (i in symptoms.indices) {
                        if (reportDocument.getBoolean(symptoms[i]) == true) {
                            checkedSymptomList.add(symptomNames[i])
                        }
                    }

                    checkedSymptoms.text = checkedSymptomList.joinToString(", ")

//                    photo?.let {
//                    }

                    textOther.text = other

                    // Fetch doctor details
                    val querySnapshot = firestore.collection("doctors")
                        .whereEqualTo("doctorId", doctorId)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val doctorDocument = querySnapshot.documents[0] // Assuming there's only one matching document
                        val firstName = doctorDocument.getString("firstName") ?: ""
                        val lastName = doctorDocument.getString("lastName") ?: ""
                        doctorFirstName.text = firstName
                        doctorLastName.text = lastName
                        doctorName.text = "$firstName $lastName"
                    } else {
                        doctorName.text = ""
                    }

                } else {
                    doctorName.text = ""
                }
            } catch (e: Exception) {
                Log.e("ReportActivity", "Error fetching report details", e)
                doctorName.text = ""
            }
        }
    }
}
