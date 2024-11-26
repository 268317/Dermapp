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
import com.example.dermapp.database.Doctor
import com.example.dermapp.messages.adapter.MyAdapterMessagesPat
import com.example.dermapp.messages.adapter.RecentChatsAdapter
import com.example.dermapp.messages.adapter.SearchDoctorsAdapterPat
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MessagesPatActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton

    private lateinit var recyclerViewDoctorsList: RecyclerView
    private lateinit var adapterDoctorsList: MyAdapterMessagesPat

    private lateinit var recyclerViewRecentChats: RecyclerView
    private lateinit var adapterRecentChats: RecentChatsAdapter

    private lateinit var searchView: SearchView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchAdapter: SearchDoctorsAdapterPat
    private var allDoctorsList: MutableList<Doctor> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_messages_pat)

        recyclerViewDoctorsList = findViewById(R.id.recyclerViewMessagesPat)
        recyclerViewDoctorsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapterDoctorsList = MyAdapterMessagesPat(this, mutableListOf())
        recyclerViewDoctorsList.adapter = adapterDoctorsList

        recyclerViewRecentChats = findViewById(R.id.recyclerViewRecentChatsPat)
        recyclerViewRecentChats.layoutManager = LinearLayoutManager(this)
        adapterRecentChats = RecentChatsAdapter(this, mutableListOf()) { conversation ->
            val intent = Intent(this, NewMessagePatActivity::class.java)
            intent.putExtra("conversationId", conversation.conversationId)
            intent.putExtra("doctorId", conversation.doctorId)
            startActivity(intent)
        }
        recyclerViewRecentChats.adapter = adapterRecentChats

        backButton = findViewById<LinearLayout>(R.id.backHeaderPat).findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, StartPatActivity::class.java))
        }

        fetchRecentChats()
        setupSearch()
        fetchDoctorsList()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_messages_pat)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSearch() {
        searchView = findViewById(R.id.searchViewDoctorsPat)
        searchResultsRecyclerView = findViewById(R.id.recyclerViewSearchResults)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchDoctorsAdapterPat(this, mutableListOf()) { doctor ->
            val currentPatientId = FirebaseAuth.getInstance().currentUser?.uid ?: return@SearchDoctorsAdapterPat
            val conversationId = "${doctor.doctorId}-$currentPatientId"
            ensureConversationExists(doctor.doctorId, currentPatientId) {
                val intent = Intent(this, NewMessagePatActivity::class.java)
                intent.putExtra("conversationId", conversationId)
                intent.putExtra("doctorId", doctor.doctorId)
                startActivity(intent)
            }
        }
        searchResultsRecyclerView.adapter = searchAdapter

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

    private fun fetchRecentChats() {
        val currentPatientId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("conversation")
            .whereEqualTo("patientId", currentPatientId)
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

    private fun fetchDoctorsList() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val doctorsSnapshot = FirebaseFirestore.getInstance().collection("doctors").get().await()
                val doctorsList = doctorsSnapshot.documents.mapNotNull { it.toObject(Doctor::class.java) }
                allDoctorsList = doctorsList.toMutableList()
                launch(Dispatchers.Main) {
                    adapterDoctorsList.setDoctorsList(allDoctorsList)
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
