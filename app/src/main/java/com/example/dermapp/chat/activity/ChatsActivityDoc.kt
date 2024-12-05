package com.example.dermapp.chat.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.chat.adapter.RecentChatsAdapter
import com.example.dermapp.chat.adapter.PatientsListAdapter
import com.example.dermapp.chat.database.Conversation
import com.example.dermapp.database.Patient
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

/**
 * ChatsActivityDoc is responsible for displaying recent chats and patient lists
 * for a doctor. It also handles navigation to the messaging screen and fetching
 * data from Firebase Firestore.
 */
class ChatsActivityDoc : AppCompatActivity() {

    private lateinit var recentChatAdapter: RecentChatsAdapter
    private lateinit var patientsAdapter: PatientsListAdapter
    private lateinit var searchView: SearchView

    private lateinit var backButton: ImageButton

    // Instance of Firebase Firestore for database operations
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val chatList = mutableListOf<Conversation>()
    private val patientsList = mutableListOf<Patient>()

    // Current logged-in user's ID
    private val currentUserId by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    /**
     * Called when the activity is first created.
     * Sets up the UI, RecyclerViews, and data fetching methods.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_messages_doc)

        // Handles the back button click in the header
        val backHeader = findViewById<LinearLayout>(R.id.backHeaderDoc)
        backButton = backHeader.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }

        setupRecyclerViews()
        fetchPatients()
        fetchChats()
        setupSearchView()
    }

    /**
     * Sets up RecyclerViews for displaying recent chats and the list of patients.
     */
    private fun setupRecyclerViews() {
        // Recent Chats RecyclerView
        recentChatAdapter = RecentChatsAdapter(this, chatList, isDoctor = true)
        findViewById<RecyclerView>(R.id.recyclerViewRecentChatsDoc).apply {
            layoutManager = LinearLayoutManager(this@ChatsActivityDoc)
            adapter = recentChatAdapter
        }

        // Patients RecyclerView
        patientsAdapter = PatientsListAdapter(this, patientsList) { patient ->
            navigateToMessages(patient)
        }
        findViewById<RecyclerView>(R.id.recyclerViewMessagesDoc).apply {
            layoutManager = LinearLayoutManager(this@ChatsActivityDoc, LinearLayoutManager.HORIZONTAL, false)
            adapter = patientsAdapter
        }
    }

    /**
     * Fetches the list of patients from Firestore and updates the RecyclerView.
     */
    private fun fetchPatients() {
        firestore.collection("patients")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("ChatsActivityDoc", "Error fetching patients", error)
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    Log.d("ChatsActivityDoc", "Patients updated: ${querySnapshot.documents.size}")
                    patientsList.clear()
                    patientsList.addAll(querySnapshot.toObjects(Patient::class.java))
                    patientsAdapter.notifyDataSetChanged()
                }
            }
        patientsAdapter.notifyDataSetChanged()
    }

    /**
     * Fetches the list of recent chats for the current user and updates the RecyclerView.
     */
    private fun fetchChats() {
        firestore.collection("conversation")
            .whereArrayContains("participants", currentUserId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                chatList.clear()
                val conversations = querySnapshot.toObjects(Conversation::class.java)
                val timestampTasks = mutableListOf<Task<Pair<Conversation, Timestamp?>>>()

                // Fetch timestamps for each conversation
                for (conversation in conversations) {
                    val lastMessageId = conversation.lastMessageId
                    val task = firestore.collection("messages")
                        .document(lastMessageId)
                        .get()
                        .continueWith { task ->
                            val document = task.result
                            val timestamp = document?.getTimestamp("timestamp")
                            conversation to timestamp
                        }
                    timestampTasks.add(task)
                }

                // Wait for all tasks to complete
                Tasks.whenAllComplete(timestampTasks).addOnSuccessListener {
                    val sortedConversations = timestampTasks.mapNotNull { task ->
                        if (task.isSuccessful) task.result else null
                    }.sortedByDescending { it.second }

                    // Update chatList and notify adapter
                    chatList.addAll(sortedConversations.map { it.first })
                    recentChatAdapter.notifyDataSetChanged()
                }.addOnFailureListener { e ->
                    Log.e("ChatsActivityDoc", "Error fetching timestamps", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatsActivityDoc", "Error fetching chats", e)
            }
    }

    /**
     * Sets up the search functionality for filtering the patient list.
     */
    private fun setupSearchView() {
        searchView = findViewById(R.id.searchViewPatientsDoc)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = patientsList.filter {
                    it.firstName.contains(newText ?: "", ignoreCase = true) ||
                            it.lastName.contains(newText ?: "", ignoreCase = true)
                }
                patientsAdapter.updatePatientsList(filteredList.toMutableList())
                return true
            }
        })
    }

    /**
     * Navigates to the messaging screen for a specific patient.
     *
     * @param patient The selected patient object.
     */
    private fun navigateToMessages(patient: Patient) {
        val conversationId = if (patient.appUserId > currentUserId) {
            "${patient.appUserId}_${currentUserId}"
        } else {
            "${currentUserId}_${patient.appUserId}"
        }

        val intent = Intent(this, MessagesActivityDoc::class.java)
        intent.putExtra("patientId", patient.appUserId)
        intent.putExtra("patientName", "${patient.firstName} ${patient.lastName}")
        intent.putExtra("patientProfilePhoto", patient.profilePhoto)
        intent.putExtra("conversationId", conversationId)
        startActivity(intent)
    }
}
