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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatsActivityPat : AppCompatActivity() {

    private lateinit var recentChatAdapter: RecentChatsAdapter
    private lateinit var doctorsAdapter: DoctorsListAdapter
    private lateinit var searchView: SearchView

    private lateinit var backButton: ImageButton

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val chatList = mutableListOf<Conversation>()
    private val doctorsList = mutableListOf<Doctor>()
    private val currentUserId by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity_messages_pat)

        // Obsługa przycisku powrotu
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

    private fun fetchDoctors() {
        firestore.collection("doctors")
            .get()
            .addOnSuccessListener { querySnapshot ->
                doctorsList.clear()
                doctorsList.addAll(querySnapshot.toObjects(Doctor::class.java))
                doctorsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ChatsActivityPat", "Error fetching doctors", e)
            }
    }

    private fun fetchChats() {
        firestore.collection("conversation")
            .whereArrayContains("participants", currentUserId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                chatList.clear()
                chatList.addAll(querySnapshot.toObjects(Conversation::class.java))
                recentChatAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ChatsActivityPat", "Error fetching chats", e)
            }
    }


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
        startActivity(intent)
    }


}
