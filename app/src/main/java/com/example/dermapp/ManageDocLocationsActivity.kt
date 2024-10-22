package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.database.Doctor
import com.example.dermapp.database.Location
import com.example.dermapp.startDoctor.StartDocActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity for managing locations associated with a doctor's profile.
 */
class ManageDocLocationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var locationsAdapter: LocationsAdapter
    private lateinit var locationsList: MutableList<String>
    private lateinit var db: FirebaseFirestore
    private var currentUserUid: String? = null
    private lateinit var backButton: AppCompatImageButton
    private var currentDocId = FirebaseAuth.getInstance().currentUser?.uid
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Called when the activity is starting.
     * Initializes UI elements and sets up necessary listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations_doc)

        db = FirebaseFirestore.getInstance()
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Set up back button click listener
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.recyclerViewLocations)
        recyclerView.layoutManager = LinearLayoutManager(this)
        locationsList = mutableListOf()
        locationsAdapter = LocationsAdapter(locationsList)
        recyclerView.adapter = locationsAdapter

        // Set up button click listener for adding new location
        val addButton: Button = findViewById(R.id.bookButton)
        val editTextNewLoc: EditText = findViewById(R.id.editTextNewLoc)
        addButton.setOnClickListener {
            val newLocation = editTextNewLoc.text.toString()
            if (newLocation.isNotEmpty()) {
                addLocation(newLocation)
                editTextNewLoc.text.clear()
            }
        }

        // Load current doctor's ID and associated locations
        loadCurrentDoctorId()
    }

    /**
     * Loads the current doctor's ID from Firestore and proceeds to load associated locations.
     */
    private fun loadCurrentDoctorId() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("doctors").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val doctor = documentSnapshot.toObject(Doctor::class.java)
                    currentDocId = doctor?.doctorId
                    loadLocations()
                } else {
                    Toast.makeText(this, "Failed to load doctor information.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Loads locations associated with the current doctor from Firestore.
     */
    private fun loadLocations() {
        db.collection("locations")
            .whereEqualTo("doctorId", currentDocId)
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

    /**
     * Adds a new location for the current doctor to Firestore.
     * @param address The full address of the new location to add.
     */
    private fun addLocation(address: String) {
        val addressPattern = Regex("""^[A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ0-9\s\.\-]+ \d+[A-Za-z]?,\s\d{2}-\d{3}\s[A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ\s]+${'$'}""")

        if (!addressPattern.matches(address)) {
            Toast.makeText(this, "Please enter the correct address format", Toast.LENGTH_LONG).show()
            return
        }
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