package com.example.dermapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dermapp.database.MedicalReport
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest
import com.example.dermapp.database.Doctor
import java.io.FileNotFoundException
import com.bumptech.glide.Glide

/**
 * Activity to display medical reports for patients.
 */
class ReportActivity : AppCompatActivity() {
    private lateinit var textViewDoctor: TextView
    private lateinit var checkBoxItching: CheckBox
    private lateinit var checkBoxMoleChanges: CheckBox
    private lateinit var checkBoxRash: CheckBox
    private lateinit var checkBoxDryness: CheckBox
    private lateinit var checkBoxPimples: CheckBox
    private lateinit var checkBoxSevereAcne: CheckBox
    private lateinit var checkBoxBlackheads: CheckBox
    private lateinit var checkBoxWarts: CheckBox
    private lateinit var checkBoxRedness: CheckBox
    private lateinit var checkBoxDiscoloration: CheckBox
    private lateinit var checkBoxSeborrhoea: CheckBox
    private lateinit var checkBoxNewMole: CheckBox
    private lateinit var enterOtherInfoEditText: EditText
    private lateinit var addPhotoImageView: ImageView
    private lateinit var backButton: ImageButton
    private var photoUri: Uri? = null
    private lateinit var firestore: FirebaseFirestore

    private lateinit var medicalReportId: String // Property to store medicalReportId

    companion object {
        const val MEDICAL_REPORT_ID_EXTRA = "medicalReportId"
        const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        firestore = FirebaseFirestore.getInstance()

        // Initialize views and setup back button click listener
        initializeViews()
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        // Navigate back to patient start activity on back button click
        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        // Retrieve medical report ID passed from previous activity
        medicalReportId = intent.getStringExtra(MEDICAL_REPORT_ID_EXTRA) ?: ""

        // Fetch medical report data from Firestore based on the ID
        fetchMedicalReportFromFirestore(medicalReportId)
    }

    /**
     * Initializes all UI views from XML layout.
     */
    private fun initializeViews() {
        textViewDoctor = findViewById(R.id.TextViewDoctor)
        checkBoxItching = findViewById(R.id.checkBoxItchingCreateNewReport)
        checkBoxMoleChanges = findViewById(R.id.checkBoxMoleChangesCreateNewReport)
        checkBoxRash = findViewById(R.id.checkBoxRashCreateNewReport)
        checkBoxDryness = findViewById(R.id.checkBoxDrynessCreateNewReport)
        checkBoxPimples = findViewById(R.id.checkBoxPimplesCreateNewReport)
        checkBoxSevereAcne = findViewById(R.id.checkBoxSevereAcneCreateNewReport)
        checkBoxBlackheads = findViewById(R.id.checkBoxBlackheadsCreateNewReport)
        checkBoxWarts = findViewById(R.id.checkBoxWartsCreateNewReport)
        checkBoxRedness = findViewById(R.id.checkBoxRednessCreateNewReport)
        checkBoxDiscoloration = findViewById(R.id.checkBoxDiscolorationCreateNewReport)
        checkBoxSeborrhoea = findViewById(R.id.checkBoxSeborrhoeaCreateNewReport)
        checkBoxNewMole = findViewById(R.id.checkBoxNewMoleCreateNewReport)
        enterOtherInfoEditText = findViewById(R.id.enterOtherInfoCreateNewReport)
        addPhotoImageView = findViewById(R.id.imageAddPhotoCreateNewReport)
    }

    /**
     * Fetches the medical report details from Firestore based on the provided medicalReportId.
     * @param medicalReportId The ID of the medical report to fetch.
     */
    private fun fetchMedicalReportFromFirestore(medicalReportId: String) {
        val db = FirebaseFirestore.getInstance()
        Log.d(TAG, "medicalReportId: ${medicalReportId}")
        val reportRef = db.collection("report").document(medicalReportId)

        reportRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val medicalReport = document.toObject(MedicalReport::class.java)

                    medicalReport?.let { report ->
                        Log.d(TAG, "Medical report fetched: $report")

                        // Fetch doctor details based on doctorId from Firestore
                        val doctorId = report.doctorId
                        Log.d(TAG, "Doctor Id: ${doctorId}")
                        if (doctorId != null && doctorId.isNotEmpty()) {
                            val db = FirebaseFirestore.getInstance()
                            val doctorsRef = db.collection("doctors")
                                .whereEqualTo("doctorId", doctorId)
                                .limit(1)

                            doctorsRef.get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val doctorDocument = querySnapshot.documents[0]
                                        val doctor = doctorDocument.toObject(Doctor::class.java)

                                        doctor?.let {
                                            // Display doctor's full name in TextView
                                            Log.d(TAG, "Doctor fetched: ${doctor.firstName} ${doctor.lastName}")
                                            textViewDoctor.text = "${doctor.firstName} ${doctor.lastName}"
                                        } ?: run {
                                            Log.e(TAG, "Doctor object is null")
                                        }
                                    } else {
                                        Log.e(TAG, "Doctor document does not exist")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(TAG, "Error getting documents: ", exception)
                                    // Handle any potential error
                                }
                        }

                        // Display medical report details in checkboxes and text fields
                        checkBoxItching.isChecked = report.itching
                        checkBoxItching.isEnabled = false
                        checkBoxMoleChanges.isChecked = report.moleChanges
                        checkBoxMoleChanges.isEnabled = false
                        checkBoxRash.isChecked = report.rash
                        checkBoxRash.isEnabled = false
                        checkBoxDryness.isChecked = report.dryness
                        checkBoxDryness.isEnabled = false
                        checkBoxPimples.isChecked = report.pimples
                        checkBoxPimples.isEnabled = false
                        checkBoxSevereAcne.isChecked = report.severeAcne
                        checkBoxSevereAcne.isEnabled = false
                        checkBoxBlackheads.isChecked = report.blackheads
                        checkBoxBlackheads.isEnabled = false
                        checkBoxWarts.isChecked = report.warts
                        checkBoxWarts.isEnabled = false
                        checkBoxRedness.isChecked = report.redness
                        checkBoxRedness.isEnabled = false
                        checkBoxDiscoloration.isChecked = report.discoloration
                        checkBoxDiscoloration.isEnabled = false
                        checkBoxSeborrhoea.isChecked = report.seborrhoea
                        checkBoxSeborrhoea.isEnabled = false
                        checkBoxNewMole.isChecked = report.newMole
                        checkBoxNewMole.isEnabled = false
                        enterOtherInfoEditText.setText(report.otherInfo)
                        enterOtherInfoEditText.isEnabled = false

                        // Fetch and display image attachment from Firebase Storage
                        try {
                            fetchImageFromFirebaseStorage(report.attachmentUrl)
                        } catch (e: SecurityException) {
                            Log.e(TAG, "SecurityException: ${e.message}")
                            Toast.makeText(this, "Unable to access the image due to security restrictions", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Handle case where medical report document does not exist
                    Toast.makeText(this@ReportActivity, "Document not found", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Medical report document not found")
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to fetch medical report document
                Toast.makeText(this@ReportActivity, "Failed to fetch document: $exception", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to fetch medical report document: $exception")
            }
    }

    /**
     * Fetches an image from Firebase Storage using the provided imageUrl and displays it in ImageView.
     * @param imageUrl The URL of the image stored in Firebase Storage.
     */
    private fun fetchImageFromFirebaseStorage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(addPhotoImageView)
    }
}