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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Conversation
import com.example.dermapp.database.Patient
import com.example.dermapp.messages.adapter.MyAdapterMessagesDoc
import com.example.dermapp.messages.adapter.RecentChatsAdapter
import com.example.dermapp.messages.adapter.SearchPatientsAdapterDoc
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MessagesDocActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton

    private lateinit var recyclerViewPatientsList: RecyclerView
    private lateinit var adapterPatientsList: MyAdapterMessagesDoc

    private lateinit var recyclerViewRecentChats: RecyclerView
    private lateinit var adapterRecentChats: RecentChatsAdapter

    private lateinit var searchView: SearchView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchAdapter: SearchPatientsAdapterDoc
    private var allPatientsList: MutableList<Patient> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_messages_doc)

        recyclerViewPatientsList = findViewById(R.id.recyclerViewMessagesDoc)
        recyclerViewPatientsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapterPatientsList = MyAdapterMessagesDoc(this, mutableListOf())
        recyclerViewPatientsList.adapter = adapterPatientsList

        recyclerViewRecentChats = findViewById(R.id.recyclerViewRecentChatsDoc)
        recyclerViewRecentChats.layoutManager = LinearLayoutManager(this)
        adapterRecentChats = RecentChatsAdapter(this, mutableListOf()) { conversation ->
            val intent = Intent(this, NewMessageDocActivity::class.java)
            intent.putExtra("conversationId", conversation.conversationId)
            intent.putExtra("patientId", conversation.patientId)
            startActivity(intent)
        }
        recyclerViewRecentChats.adapter = adapterRecentChats

        backButton = findViewById<LinearLayout>(R.id.backHeaderDoc).findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, StartDocActivity::class.java))
        }

        fetchRecentChats()
        setupSearch()
        fetchPatientsList()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_messages_doc)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSearch() {
        searchView = findViewById(R.id.searchViewPatientsDoc)
        searchResultsRecyclerView = findViewById(R.id.recyclerViewSearchResultsDoc)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchPatientsAdapterDoc(this, mutableListOf()) { patient ->
            val currentDoctorId = FirebaseAuth.getInstance().currentUser?.uid ?: return@SearchPatientsAdapterDoc
            val conversationId = "$currentDoctorId-${patient.appUserId}"
            ensureConversationExists(currentDoctorId, patient.appUserId) {
                val intent = Intent(this, NewMessageDocActivity::class.java)
                intent.putExtra("conversationId", conversationId)
                intent.putExtra("patientId", patient.appUserId)
                startActivity(intent)
            }
        }
        searchResultsRecyclerView.adapter = searchAdapter

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

    private fun fetchRecentChats() {
        val currentDoctorId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("conversation")
            .whereEqualTo("doctorId", currentDoctorId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    e.printStackTrace()
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val conversations = snapshots.documents.mapNotNull { it.toObject(Conversation::class.java) }
                        .filter { !it.lastMessage.isNullOrEmpty() }
                    adapterRecentChats.updateConversations(conversations.toMutableList())
                }
            }
    }

    private fun fetchPatientsList() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val patientsSnapshot = FirebaseFirestore.getInstance().collection("patients").get().await()
                val patientsList = patientsSnapshot.documents.mapNotNull { it.toObject(Patient::class.java) }
                allPatientsList = patientsList.toMutableList()
                launch(Dispatchers.Main) {
                    adapterPatientsList.setPatientsList(allPatientsList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun ensureConversationExists(doctorId: String, patientId: String, onComplete: (String) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val conversationRef = firestore.collection("conversation")
        val conversationId = "$doctorId-$patientId"

        conversationRef.document(conversationId).get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val newConversation = mapOf(
                    "conversationId" to conversationId,
                    "doctorId" to doctorId,
                    "patientId" to patientId,
                    "lastMessage" to "",
                    "lastMessageTimestamp" to null
                )
                conversationRef.document(conversationId).set(newConversation).addOnSuccessListener {
                    onComplete(conversationId)
                }
            } else {
                onComplete(conversationId)
            }
        }
    }
}
