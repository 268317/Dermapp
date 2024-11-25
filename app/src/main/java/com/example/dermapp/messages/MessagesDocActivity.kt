package com.example.dermapp.messages

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Patient
import com.example.dermapp.messages.adapter.MyAdapterMessagesDoc
import com.example.dermapp.messages.adapter.SearchPatientsAdapterDoc
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    private lateinit var searchView: SearchView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchAdapter: SearchPatientsAdapterDoc
    private var allPatientsList: MutableList<Patient> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_messages_doc)

        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.recyclerViewMessagesDoc)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = MyAdapterMessagesDoc(this, mutableListOf())
        recyclerView.adapter = adapter

        // Setup back button click listener to navigate back to StartDocActivity
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

        // Search View and RecyclerView setup
        searchView = findViewById(R.id.searchViewPatientsDoc)
        searchResultsRecyclerView = findViewById(R.id.recyclerViewSearchResultsDoc)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchPatientsAdapterDoc(this, mutableListOf()) { patient ->
            // Ensure conversation exists or create a new one, then navigate
            ensureConversationExists(patient.appUserId) { conversationId ->
                val intent = Intent(this, NewMessageDocActivity::class.java)
                intent.putExtra("conversationId", conversationId) // Pass the conversation ID
                intent.putExtra("receiverId", patient.appUserId) // Pass the receiver ID
                startActivity(intent)
            }
        }
        searchResultsRecyclerView.adapter = searchAdapter

        // Fetch doctors list and populate RecyclerView
        fetchPatientsList()
        fetchPatientsList2()

        // Search listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = allPatientsList.filter {
                    it.firstName.contains(newText ?: "", ignoreCase = true) ||
                            it.lastName.contains(newText ?: "", ignoreCase = true)
                }
                searchAdapter.setPatientsList(filteredList.toMutableList())
                searchResultsRecyclerView.visibility = if (filteredList.isEmpty()) View.GONE else View.VISIBLE
                return true
            }
        })
    }

    /**
     * Ensures that a conversation exists between the current user and the given doctor.
     * If it does not exist, a new conversation is created.
     */
    private fun ensureConversationExists(receiverId: String, onComplete: (conversationId: String) -> Unit) {
        val conversationRef = FirebaseFirestore.getInstance().collection("conversations")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Check if a conversation already exists
        conversationRef
            .whereEqualTo("senderId", currentUserId)
            .whereEqualTo("receiverId", receiverId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Existing conversation found
                    val existingConversationId = querySnapshot.documents[0].id
                    onComplete(existingConversationId)
                } else {
                    // Create a new conversation
                    val newConversationId = conversationRef.document().id
                    val newConversation = mapOf(
                        "conversationId" to newConversationId,
                        "senderId" to currentUserId,
                        "receiverId" to receiverId,
                        "lastMessage" to "",
                        "lastMessageTimestamp" to com.google.firebase.Timestamp.now()
                    )
                    conversationRef.document(newConversationId).set(newConversation)
                        .addOnSuccessListener {
                            onComplete(newConversationId)
                        }
                        .addOnFailureListener { exception ->
                            exception.printStackTrace()
                        }
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    /**
     * Fetches the list of patients from Firestore and updates the RecyclerView adapter.
     */
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
                            it.appUserId = document.id // Assign Firestore document ID to patientId field
                            patientsList.add(it)
                        }
                    }

                    // Set patients list in the RecyclerView adapter
                    adapter.setPatientsList(patientsList)

                } catch (e: Exception) {
                    // Handle Firestore fetch errors
                }
            }
        }
    }

    private fun fetchPatientsList2() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val doctorsSnapshot = FirebaseFirestore.getInstance().collection("patients").get().await()
                allPatientsList = doctorsSnapshot.documents.mapNotNull { document ->
                    document.toObject(Patient::class.java)?.apply { appUserId = document.id }
                }.toMutableList()
                adapter.setPatientsList(allPatientsList) // Main adapter
            } catch (e: Exception) {
                // Handle errors
            }
        }
    }
}