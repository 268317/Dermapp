package com.example.dermapp.messages

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Patient
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Activity for displaying messages and interacting with patients as a doctor.
 */
class MessagesDocActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapterMessagesDoc

    /**
     * Initializes the activity, sets up RecyclerView, and prepares UI elements.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages_doc)

        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.recyclerViewMessagesDoc)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = MyAdapterMessagesDoc(this, mutableListOf())
        recyclerView.adapter = adapter

        // Setup back button click listener to navigate back to StartPatActivity
        val header = findViewById<LinearLayout>(R.id.backHeaderDoc)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }

        // Apply system bars insets to the main layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_messages_doc)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Fetch doctors list and populate RecyclerView
        fetchPatientsList()
    }

    /**
     * Fetches the list of patients from Firestore and updates the RecyclerView adapter.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchPatientsList() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        currentUserUid?.let { uid ->
            val patientsList = mutableListOf<Patient>()

            // Fetch patients from Firestore using coroutines
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val patientsSnapshot = FirebaseFirestore.getInstance().collection("patients").get().await()

                    for (document in patientsSnapshot.documents) {
                        val patient = document.toObject(Patient::class.java)
                        patient?.let {
                            it.appUserId = document.id // Assign Firestore document ID to doctorId field
                            patientsList.add(it)
                        }
                    }

                    // Set doctors list in the RecyclerView adapter
                    adapter.setPatientsList(patientsList)

                } catch (e: Exception) {
                    // Handle Firestore fetch errors
                    // For example:
                    // Log.e(TAG, "Error fetching doctors", e)
                    // Toast.makeText(this@MessagesPatActivity, "Error fetching doctors", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
