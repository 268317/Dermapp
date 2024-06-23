package com.example.dermapp


import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.database.Doctor
import com.example.dermapp.database.Location
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ManageDocLocationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var locationsAdapter: LocationsAdapter
    private lateinit var locationsList: MutableList<String>
    private lateinit var db: FirebaseFirestore
    private var currentUserUid: String? = null
    private lateinit var backButton: AppCompatImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations_doc)

        db = FirebaseFirestore.getInstance()
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerViewLocations)
        recyclerView.layoutManager = LinearLayoutManager(this)
        locationsList = mutableListOf()
        locationsAdapter = LocationsAdapter(locationsList)
        recyclerView.adapter = locationsAdapter

        val addButton: Button = findViewById(R.id.bookButton)
        val editTextNewLoc: EditText = findViewById(R.id.editTextNewLoc)

        addButton.setOnClickListener {
            val newLocation = editTextNewLoc.text.toString()
            if (newLocation.isNotEmpty()) {
                addLocation(newLocation)
                editTextNewLoc.text.clear()
            }
        }

        loadLocations()
    }

    private fun loadLocations() {
        Log.d(TAG, "I want to load locations")
        db.collection("locations")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.getString("fullAddress")?.let {
                        locationsList.add(it)
                        Log.d("loclocloc", "${it}")
                    }
                }
                locationsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("ManageLocationsActivity", "Error getting documents: ", exception)
            }
    }

    private fun addLocation(address: String) {
        val userRef = db.collection("doctors").document(currentUserUid!!)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(Doctor::class.java)
                user?.let {
                    val doctorId = user.doctorId
                    val newLocation = hashMapOf(
                        "fullAddress" to address,
                        "doctorId" to doctorId
                    )

                    db.collection("locations").add(newLocation)
                        .addOnSuccessListener { documentReference ->
                            val generatedLocationId = documentReference.id
                            // Update the document with the generated locationId
                            documentReference.update("locationId", generatedLocationId)
                                .addOnSuccessListener {
                                    val updatedLocation = Location(
                                        fullAddress = address,
                                        doctorId = doctorId,
                                        locationId = generatedLocationId
                                    )
                                    locationsList.add(updatedLocation.fullAddress)
                                    locationsAdapter.notifyItemInserted(locationsList.size - 1)
                                }
                                .addOnFailureListener { e ->
                                    Log.w("ManageLocationsActivity", "Error updating document with locationId", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.w("ManageLocationsActivity", "Error adding document", e)
                        }
                }
            }
        }
    }
}