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
import com.example.dermapp.chat.adapter.DoctorsListAdapter
import com.example.dermapp.chat.database.Conversation
import com.example.dermapp.database.Doctor
import com.example.dermapp.startPatient.StartPatActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * ChatsActivityPat is responsible for displaying recent chats and a list of doctors
 * for a patient. It also handles navigation to the messaging screen and fetching
 * data from Firebase Firestore.
 */
class ChatsActivityPat : AppCompatActivity() {

    private lateinit var recentChatAdapter: RecentChatsAdapter
    private lateinit var doctorsAdapter: DoctorsListAdapter
    private lateinit var searchView: SearchView

    private lateinit var backButton: ImageButton

    // Instance of Firebase Firestore for database operations
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val chatList = mutableListOf<Conversation>()
    private val doctorsList = mutableListOf<Doctor>()

    // Current logged-in user's ID
    private val currentUserId by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    /**
     * Called when the activity is first created.
     * Sets up the UI, RecyclerViews, and data fetching methods.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_messages_pat)

        // Handles the back button click in the header
        val backHeader = findViewById<LinearLayout>(R.id.backHeaderPat)
        backButton = backHeader.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        setupRecyclerViews()
        fetchDoctors()
        fetchChats()
        setupSearchView()
    }

    /**
     * Sets up RecyclerViews for displaying recent chats and the list of doctors.
     */
    private fun setupRecyclerViews() {
        recentChatAdapter = RecentChatsAdapter(this, chatList, isDoctor = false)
        findViewById<RecyclerView>(R.id.recyclerViewRecentChats).apply {
            layoutManager = LinearLayoutManager(this@ChatsActivityPat)
            adapter = recentChatAdapter
        }

        doctorsAdapter = DoctorsListAdapter(this, doctorsList) { doctor ->
            navigateToMessages(doctor)
        }
        findViewById<RecyclerView>(R.id.recyclerViewMessagesPat).apply {
            layoutManager = LinearLayoutManager(this@ChatsActivityPat, LinearLayoutManager.HORIZONTAL, false)
            adapter = doctorsAdapter
        }
    }

    /**
     * Fetches the list of doctors from Firestore and updates the RecyclerView.
     */
    private fun fetchDoctors() {
        firestore.collection("doctors")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("ChatsActivityPat", "Error fetching doctors", error)
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    Log.d("ChatsActivityPat", "Doctors updated: ${querySnapshot.documents.size}")
                    doctorsList.clear()
                    for (document in querySnapshot.documents) {
                        val doctor = document.toObject(Doctor::class.java)
                        Log.d("ChatsActivityPat", "Doctor fetched: ${doctor?.appUserId}")
                        doctor?.let { doctorsList.add(it) }
                    }
                    doctorsAdapter.notifyDataSetChanged()
                }
            }
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
     * Sets up the search functionality for filtering the doctor list.
     */
    private fun setupSearchView() {
        searchView = findViewById(R.id.searchViewDoctorsPat)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = doctorsList.filter {
                    it.firstName.contains(newText ?: "", ignoreCase = true) ||
                            it.lastName.contains(newText ?: "", ignoreCase = true)
                }
                doctorsAdapter.updateDoctorsList(filteredList.toMutableList())
                return true
            }
        })
    }

    /**
     * Navigates to the messaging screen for a specific doctor.
     *
     * @param doctor The selected doctor object.
     */
    private fun navigateToMessages(doctor: Doctor) {
        val conversationId = if (doctor.appUserId > currentUserId) {
            "${doctor.appUserId}_${currentUserId}"
        } else {
            "${currentUserId}_${doctor.appUserId}"
        }

        val intent = Intent(this, MessagesActivityPat::class.java)
        intent.putExtra("doctorId", doctor.appUserId)
        intent.putExtra("doctorName", "${doctor.firstName} ${doctor.lastName}")
        intent.putExtra("doctorProfilePhoto", doctor.profilePhoto)
        intent.putExtra("conversationId", conversationId)
        Log.d("ChatsActivityPat", "Doctor ID: ${doctor.appUserId}")
        Log.d("ChatsActivityPat", "Conversation ID: $conversationId")
        startActivity(intent)
    }
}
