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
import com.example.dermapp.database.Doctor
import com.example.dermapp.messages.adapter.MyAdapterMessagesPat
import com.example.dermapp.messages.adapter.SearchDoctorsAdapterPat
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MessagesPatActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapterMessagesPat

    private lateinit var searchView: SearchView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchAdapter: SearchDoctorsAdapterPat
    private var allDoctorsList: MutableList<Doctor> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_messages_pat)

        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.recyclerViewMessagesPat)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = MyAdapterMessagesPat(this, mutableListOf())
        recyclerView.adapter = adapter

        // Setup back button click listener to navigate back to StartPatActivity
        val header = findViewById<LinearLayout>(R.id.backHeaderPat)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        // Apply system bars insets to the main layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_messages_pat)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Search View and RecyclerView setup
        searchView = findViewById(R.id.searchViewDoctorsPat)
        searchResultsRecyclerView = findViewById(R.id.recyclerViewSearchResults)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchDoctorsAdapterPat(this, mutableListOf()) { doctor ->
            // Ensure conversation exists or create a new one, then navigate
            ensureConversationExists(doctor.doctorId) { conversationId ->
                val intent = Intent(this, NewMessagePatActivity::class.java)
                intent.putExtra("conversationId", conversationId) // Pass the conversation ID
                intent.putExtra("receiverId", doctor.doctorId) // Pass the receiver ID
                startActivity(intent)
            }
        }
        searchResultsRecyclerView.adapter = searchAdapter

        // Fetch doctors list and populate RecyclerView
        fetchDoctorsList()
        fetchDoctorsList2()

        // Search listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = allDoctorsList.filter {
                    it.firstName.contains(newText ?: "", ignoreCase = true) ||
                            it.lastName.contains(newText ?: "", ignoreCase = true)
                }
                searchAdapter.setDoctorsList(filteredList.toMutableList())
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
     * Fetches the list of doctors from Firestore and updates the RecyclerView adapter.
     */
    private fun fetchDoctorsList() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        currentUserUid?.let { uid ->
            val doctorsList = mutableListOf<Doctor>()

            // Fetch doctors from Firestore using coroutines
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val doctorsSnapshot = FirebaseFirestore.getInstance().collection("doctors").get().await()

                    for (document in doctorsSnapshot.documents) {
                        val doctor = document.toObject(Doctor::class.java)
                        doctor?.let {
                            it.doctorId = document.id // Assign Firestore document ID to doctorId field
                            doctorsList.add(it)
                        }
                    }

                    // Set doctors list in the RecyclerView adapter
                    adapter.setDoctorsList(doctorsList)

                } catch (e: Exception) {
                    // Handle Firestore fetch errors
                }
            }
        }
    }

    private fun fetchDoctorsList2() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val doctorsSnapshot = FirebaseFirestore.getInstance().collection("doctors").get().await()
                allDoctorsList = doctorsSnapshot.documents.mapNotNull { document ->
                    document.toObject(Doctor::class.java)?.apply { doctorId = document.id }
                }.toMutableList()
                adapter.setDoctorsList(allDoctorsList) // Main adapter
            } catch (e: Exception) {
                // Handle errors
            }
        }
    }
}
