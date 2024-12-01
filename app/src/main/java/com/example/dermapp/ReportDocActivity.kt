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
import com.google.firebase.firestore.FirebaseFirestore
import com.example.dermapp.database.Doctor
import java.io.FileNotFoundException
import com.bumptech.glide.Glide
import com.example.dermapp.startDoctor.StartDocActivity
import android.Manifest

/**
 * Activity to display medical reports for doctors.
 */
class ReportDocActivity : AppCompatActivity() {
    private lateinit var textViewPatient: TextView
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

    private lateinit var medicalReportId: String // Property to store medicalReportId

    companion object {
        const val MEDICAL_REPORT_ID_EXTRA = "medicalReportId"
        const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_doc)

        // Initialize views and setup permissions for image loading
        initializeViews()
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        // Handle back button click to return to previous activity
        backButton.setOnClickListener {
            val intent = Intent(this, StartDocActivity::class.java)
            startActivity(intent)
        }

        // Retrieve medical report ID passed from previous activity
        medicalReportId = intent.getStringExtra(MEDICAL_REPORT_ID_EXTRA) ?: ""

        // Fetch medical report data from Firestore based on the ID
        fetchMedicalReportFromFirestore(medicalReportId)
    }

    /**
     * Initializes all UI views from XML layout and checks permissions for image loading.
     */
    private fun initializeViews() {
        textViewPatient = findViewById(R.id.TextViewPatient)
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

        // Request permission to read external storage if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_EXTERNAL_STORAGE
            )
        } else {
            // Load image if permission is already granted
            loadImage()
        }
    }

    /**
     * Loads an image from external storage using its URI and displays it in an ImageView.
     */
    private fun loadImage() {
        try {
            // Example URI to be replaced with actual URI from Firestore or other source
            val imageUri = Uri.parse("content://media/external/images/media/1000035185")
            val inputStream = contentResolver.openInputStream(imageUri)
            if (inputStream != null) {
                addPhotoImageView.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Unable to access the image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}")
            Toast.makeText(this, "Unable to access the image due to security restrictions", Toast.LENGTH_SHORT).show()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "FileNotFoundException: ${e.message}")
            Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show()
        }
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

                        // Fetch patient details based on PESEL from Firestore
                        val pesel = report.patientPesel
                        if (pesel != null && pesel.isNotEmpty()) {
                            val db = FirebaseFirestore.getInstance()
                            val doctorsRef = db.collection("patients")
                                .whereEqualTo("pesel", pesel)
                                .limit(1)

                            doctorsRef.get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val doctorDocument = querySnapshot.documents[0]
                                        val doctor = doctorDocument.toObject(Doctor::class.java)

                                        doctor?.let {
                                            // Display patient's full name in TextView
                                            textViewPatient.text = "${doctor.firstName} ${doctor.lastName}"
                                        } ?: run {
                                            Log.e(TAG, "Patient object is null")
                                        }
                                    } else {
                                        Log.e(TAG, "Patient document does not exist")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(TAG, "Error getting documents: ", exception)
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
                    Toast.makeText(this@ReportDocActivity, "Document not found", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Medical report document not found")
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to fetch medical report document
                Toast.makeText(this@ReportDocActivity, "Failed to fetch document: $exception", Toast.LENGTH_SHORT).show()
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