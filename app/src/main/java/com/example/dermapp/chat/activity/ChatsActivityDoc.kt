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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatsActivityDoc : AppCompatActivity() {

    private lateinit var recentChatAdapter: RecentChatsAdapter
    private lateinit var patientsAdapter: PatientsListAdapter
    private lateinit var searchView: SearchView

    private lateinit var backButton: ImageButton

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val chatList = mutableListOf<Conversation>()
    private val patientsList = mutableListOf<Patient>()
    private val currentUserId by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_messages_doc)

        // Obsługa przycisku powrotu
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

    private fun fetchPatients() {
        firestore.collection("patients")
            .get()
            .addOnSuccessListener { querySnapshot ->
                patientsList.clear()
                patientsList.addAll(querySnapshot.toObjects(Patient::class.java))
                patientsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ChatsActivityDoc", "Error fetching patients", e)
            }
    }

    private fun fetchChats() {
        firestore.collection("conversation")
            .whereEqualTo("doctorId", currentUserId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                chatList.clear()
                chatList.addAll(querySnapshot.toObjects(Conversation::class.java))
                recentChatAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ChatsActivityDoc", "Error fetching chats", e)
            }
    }

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
