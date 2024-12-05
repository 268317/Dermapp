package com.example.dermapp

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.dermapp.database.Doctor
import com.example.dermapp.database.MedicalReport
import com.example.dermapp.database.Patient
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Activity to create and send a new medical report.
 * The user can select symptoms, upload photos, and associate the report with a doctor.
 */
class CreateNewReportActivity : AppCompatActivity() {

    // UI elements and other variables declaration
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
    private lateinit var addPhotoTextView: TextView
    private lateinit var addPhotoImageView: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var autoDoc: AutoCompleteTextView

    private var photoUri: Uri? = null
    private var selectedDoctorId: String? = null

    private val PICK_IMAGE_REQUEST = 1
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var doctorsList: List<Doctor>

    private lateinit var photoFile: File

    /**
     * Called when the activity is starting.
     * Sets up UI elements, initializes listeners, and retrieves necessary data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_report)

        // Set time zone for the activity
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        // Initialize UI elements and perform other setup operations
        initializeUI()

        // Send Report Button click listener
        val sendReportButton = findViewById<Button>(R.id.buttonUpdateProfilePat)
        sendReportButton.setOnClickListener {
            // Upload selected photo to Firebase Storage
            uploadPhotoToFirebaseStorage()
        }

        // Handle click on ImageView to add a photo
        addPhotoImageView.setOnClickListener {
            showImageSelectionDialog()
        }

        // Fetch list of doctors from Firestore and populate AutoCompleteTextView
        val doctorsCollection = FirebaseFirestore.getInstance().collection("doctors")
        doctorsCollection.get().addOnSuccessListener { doctorsResult ->
            doctorsList = doctorsResult.toObjects(Doctor::class.java)
            val doctorNames = doctorsList.map { "${it.lastName} ${it.firstName}" }.toTypedArray()
            val docAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, doctorNames)
            autoDoc.setAdapter(docAdapter)
        }

        // Load doctors list initially
        loadDoctors()
    }

    /**
     * Initializes UI components.
     * Binds UI elements from XML layout, sets up click listeners, and AutoCompleteTextView adapter.
     */
    private fun initializeUI() {
        // Initialize UI elements
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
        autoDoc = findViewById(R.id.autoCompleteTextViewDoctor)

        // Back button setup
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        // Initialize AutoCompleteTextView for doctors
        setupAutoCompleteTextView()
    }

    /**
     * Sets up the AutoCompleteTextView for doctors selection.
     * Handles item selection to get the selected doctor's ID.
     */
    private fun setupAutoCompleteTextView() {
        autoDoc.setOnItemClickListener { _, _, position, _ ->
            val selectedDoctorText = autoDoc.text.toString()
            val selectedDoctor = doctorsList.find { "${it.firstName} ${it.lastName}" == selectedDoctorText }
            selectedDoctor?.let {
                selectedDoctorId = it.doctorId
            } ?: run {
                Log.e(TAG, "Couldn't find doctor in the list")
            }
        }
    }

    /**
     * Opens the device's image gallery to select a photo.
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    /**
     * Handles the result of selecting an image from the gallery.
     * Updates the UI with the selected image.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null -> {
                photoUri = data.data
                addPhotoImageView.setImageURI(photoUri)
            }
            requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK -> {
                // User took a photo
                val savedPhotoUri = Uri.fromFile(photoFile) // Use photoFile from createImageFile
                addPhotoImageView.setImageURI(savedPhotoUri)
                photoUri = savedPhotoUri // Save URI for uploading to Firebase
            }
        }
    }

    private val REQUEST_IMAGE_CAPTURE = 2

    /**
     * Opens the camera to take a new photo for the profile image.
     */
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            photoFile = createImageFile() ?: return
            val photoURI: Uri = FileProvider.getUriForFile(this,
                "${packageName}.provider",
                photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    /**
     * Creates a temporary image file for storing the photo taken from the camera.
     * @return The created image file.
     */
    private fun createImageFile(): File? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timestamp}_",
            ".jpg",
            storageDir
        ).apply {
            photoUri = Uri.fromFile(this) // Set URI for the saved photo
        }
    }

    /**
     * Uploads the selected photo to Firebase Storage.
     * Handles success and failure cases of the upload.
     */
    private fun uploadPhotoToFirebaseStorage() {
        photoUri?.let { uri ->
            val storageRef = FirebaseStorage.getInstance().reference
            val photoRef = storageRef.child("reports/${UUID.randomUUID()}")
            val uploadTask = photoRef.putFile(uri)
            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    photoRef.downloadUrl.addOnSuccessListener { url ->
                        saveReport(url.toString())
                    }
                } else {
                    Toast.makeText(this, "Failed to upload photo: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(this, "Please select a photo to upload", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Saves the medical report to Firestore after uploading the photo.
     * Retrieves current user's information and creates a new MedicalReport object.
     * Handles success and failure cases of saving the report.
     */
    private fun saveReport(photoUrl: String) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseFirestore.getInstance().collection("patients").document(currentUserUid!!)

        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(Patient::class.java)
                user?.let {
                    val pesel = user.pesel

                    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw")

                    val calendar = Calendar.getInstance()
                    val currentDate = calendar.time
                    val currentDateString = dateFormat.format(currentDate)
                    val doctorId = selectedDoctorId ?: return@addOnSuccessListener

                    val report = MedicalReport(
                        doctorId = doctorId,
                        patientPesel = pesel,
                        date = currentDateString,
                        itching = checkBoxItching.isChecked,
                        rash = checkBoxRash.isChecked,
                        redness = checkBoxRedness.isChecked,
                        newMole = checkBoxNewMole.isChecked,
                        moleChanges = checkBoxMoleChanges.isChecked,
                        blackheads = checkBoxBlackheads.isChecked,
                        pimples = checkBoxPimples.isChecked,
                        warts = checkBoxWarts.isChecked,
                        dryness = checkBoxDryness.isChecked,
                        severeAcne = checkBoxSevereAcne.isChecked,
                        seborrhoea = checkBoxSeborrhoea.isChecked,
                        discoloration = checkBoxDiscoloration.isChecked,
                        otherInfo = enterOtherInfoEditText.text.toString(),
                        attachmentUrl = photoUrl
                    )

                    firestore.collection("report")
                        .add(report)
                        .addOnSuccessListener { documentReference ->
                            val generatedReportId = documentReference.id
                            val updatedReport = report.copy(medicalReportId = generatedReportId)
                            documentReference.set(updatedReport)
                            Toast.makeText(this, "Report sent successfully.", Toast.LENGTH_SHORT).show()
                            clearFields()
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to add report: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }

    /**
     * Clears all input fields and resets UI elements after sending a report.
     */
    private fun clearFields() {
        autoDoc.setText("")
        selectedDoctorId = null
        checkBoxItching.isChecked = false
        checkBoxMoleChanges.isChecked = false
        checkBoxRash.isChecked = false
        checkBoxDryness.isChecked = false
        checkBoxPimples.isChecked = false
        checkBoxSevereAcne.isChecked = false
        checkBoxBlackheads.isChecked = false
        checkBoxWarts.isChecked = false
        checkBoxRedness.isChecked = false
        checkBoxDiscoloration.isChecked = false
        checkBoxSeborrhoea.isChecked = false
        checkBoxNewMole.isChecked = false
        enterOtherInfoEditText.text.clear()
        addPhotoTextView.text = ""
        addPhotoImageView.setImageURI(null)
    }

    /**
     * Loads the list of doctors from Firestore.
     * Updates the AutoCompleteTextView with the list of doctor names.
     */
    private fun loadDoctors() {
        val doctorsCollection = firestore.collection("doctors")
        doctorsCollection.get()
            .addOnSuccessListener { doctorsResult ->
                val doctorsList = doctorsResult.toObjects(Doctor::class.java)
                val doctorNames = doctorsList.map { "${it.firstName} ${it.lastName}" }.toTypedArray()
                val docAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, doctorNames)
                autoDoc.setAdapter(docAdapter)
                setupAutoCompleteTextView()
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("New photo", "Open gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose option")

        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    openCamera()
                }
                1 -> {
                    openGallery()
                }
            }
        }

        builder.show()
    }
}
