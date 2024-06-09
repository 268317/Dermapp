package com.example.dermapp.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.StartDocActivity
import com.example.dermapp.database.Patient
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessagesDocActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages_doc)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMessagesDoc)
        val items: MutableList<Patient> = ArrayList()

//        // Firebase database reference
//        val database = FirebaseDatabase.getInstance()
//        val patientsRef = database.getReference("patients")
//
//        // Retrieve data from Firebase
//        patientsRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                items.clear()
//                for (patientSnapshot in snapshot.children) {
//                    val patient = patientSnapshot.getValue(Patient::class.java)
//                    if (patient != null) {
//                        items.add(patient)
//                    }
//                }
//                recyclerView.adapter?.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.w("MessagesDocActivity", "loadPatients:onCancelled", error.toException())
//                showToast("Failed to load patients.")
//            }
//        })


        items.add(
            Patient(
                appUserId = "1234",
                email = "doctor1@gmail.com",
                password = "123456",
                firstName = "Doctor",
                lastName = "Doctor 1",
                birthDate = "12.",
                pesel = "12345678910"
            )
        )


        val header = findViewById<LinearLayout>(R.id.backHeaderDoc)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }

//        // Retrieve views
//        val imageViewAddMessagesDoc = findViewById<ImageView>(R.id.imageViewAddMessagesDoc)
//
//        // Set click listener for adding messages
//        imageViewAddMessagesDoc.setOnClickListener {
//            // Implement your logic for adding new messages
//            showToast("Add new message clicked")
//        }

        // Set layout manager and adapter for RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyAdapterMessagesDoc(items)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setPatientEntryClickListener(imageViewId: Int, textViewId: Int, patientName: String) {
        val imageView = findViewById<ImageView>(imageViewId)
        val textView = findViewById<TextView>(textViewId)

        imageView.setOnClickListener {
            // Handle click on patient image
            showToast("Clicked on $patientName's image")
        }

        textView.setOnClickListener {
            // Handle click on patient name
            showToast("Clicked on $patientName's name")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
