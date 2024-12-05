package com.example.dermapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.database.MedicalReport
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.dermapp.database.Doctor
import com.bumptech.glide.Glide

/**
 * Activity for displaying medical reports to patients.
 * This class allows patients to view detailed medical reports provided by their doctors,
 * including symptoms, doctor's information, additional notes, and attached images.
 */
class ReportActivity : AppCompatActivity() {

    // UI components for displaying medical report details
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
    private lateinit var firestore: FirebaseFirestore
    private var photoUri: Uri? = null

    // Stores the medical report ID passed from the previous activity
    private lateinit var medicalReportId: String

    companion object {
        const val MEDICAL_REPORT_ID_EXTRA = "medicalReportId"
    }

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up the back button, and fetches the medical report data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        firestore = FirebaseFirestore.getInstance()

        // Initialize UI components
        initializeViews()

        // Set up back button to navigate to the patient start activity
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        // Retrieve the medical report ID passed from the previous activity
        medicalReportId = intent.getStringExtra(MEDICAL_REPORT_ID_EXTRA) ?: ""

        // Fetch and display the medical report details from Firestore
        fetchMedicalReportFromFirestore(medicalReportId)
    }

    /**
     * Initializes all UI views from the XML layout.
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
     * Displays the report details and associated doctor's information.
     * @param medicalReportId The ID of the medical report to fetch.
     */
    private fun fetchMedicalReportFromFirestore(medicalReportId: String) {
        val db = FirebaseFirestore.getInstance()
        Log.d(TAG, "Fetching medical report with ID: $medicalReportId")
        val reportRef = db.collection("report").document(medicalReportId)

        reportRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val medicalReport = document.toObject(MedicalReport::class.java)

                    medicalReport?.let { report ->
                        Log.d(TAG, "Medical report fetched: $report")

                        // Fetch doctor's details based on the doctorId from the report
                        val doctorId = report.doctorId
                        if (!doctorId.isNullOrEmpty()) {
                            fetchDoctorDetails(doctorId)
                        }

                        // Populate UI with the report's details
                        populateReportDetails(report)

                        // Fetch and display the attached image from Firebase Storage
                        try {
                            fetchImageFromFirebaseStorage(report.attachmentUrl)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching image: ${e.message}")
                            Toast.makeText(this, "Unable to load the image.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Document not found", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Medical report document not found")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch document: $exception", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to fetch medical report document: $exception")
            }
    }

    /**
     * Fetches the doctor's details using their doctorId from Firestore.
     * @param doctorId The ID of the doctor to fetch details for.
     */
    private fun fetchDoctorDetails(doctorId: String) {
        val db = FirebaseFirestore.getInstance()
        val doctorRef = db.collection("doctors").whereEqualTo("doctorId", doctorId).limit(1)

        doctorRef.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val doctorDocument = querySnapshot.documents[0]
                    val doctor = doctorDocument.toObject(Doctor::class.java)

                    doctor?.let {
                        textViewDoctor.text = "${doctor.firstName} ${doctor.lastName}"
                        Log.d(TAG, "Doctor details fetched: ${doctor.firstName} ${doctor.lastName}")
                    } ?: Log.e(TAG, "Doctor object is null")
                } else {
                    Log.e(TAG, "Doctor document not found")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching doctor details: $exception")
            }
    }

    /**
     * Populates the UI components with the details from the medical report.
     * @param report The medical report to display.
     */
    private fun populateReportDetails(report: MedicalReport) {
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
    }

    /**
     * Fetches an image from Firebase Storage using the provided URL.
     * @param imageUrl The URL of the image to fetch.
     */
    private fun fetchImageFromFirebaseStorage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(addPhotoImageView)
    }
}
