package com.example.dermapp

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.dermapp.database.AppUser
import com.example.dermapp.database.Appointment
import com.example.dermapp.database.Doctor
import com.example.dermapp.database.MedicalReport
import com.example.dermapp.database.Patient
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.room.util.copy
import kotlinx.coroutines.*
import java.util.*

class CreateNewReportActivity : AppCompatActivity() {

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

    private val PICK_IMAGE_REQUEST = 1
    private var selectedDoctorId: String? = null
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var doctorsList: List<Doctor>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_report)

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        val sendReportButton = findViewById<Button>(R.id.buttonUpdateProfilePat)
        sendReportButton.setOnClickListener {
            saveReport(photoUri.toString())
        }

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
        addPhotoTextView = findViewById(R.id.textViewAddPhotoCreateNewReport)
        addPhotoImageView = findViewById(R.id.imageAddPhotoCreateNewReport)
        autoDoc = findViewById(R.id.autoCompleteTextViewDoctor)

        // Handle checkbox states or other interactions
        handleCheckboxes()

        // Handle adding photo (click listener for ImageView)
        addPhotoImageView.setOnClickListener {
            openGallery()
        }

        val doctorsCollection = FirebaseFirestore.getInstance().collection("doctors")
        doctorsCollection.get().addOnSuccessListener { doctorsResult ->
            doctorsList = doctorsResult.toObjects(Doctor::class.java)
            val doctorNames = doctorsList.map { "${it.lastName} ${it.firstName}" }.toTypedArray()
            val docAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, doctorNames)
            autoDoc.setAdapter(docAdapter)
        }

        loadDoctors()
    }

    private fun setupAutoCompleteTextView() {
        autoDoc.setOnItemClickListener { _, _, position, _ ->
            val selectedDoctorText = autoDoc.text.toString()
            val selectedDoctor = doctorsList.find { "${it.firstName} ${it.lastName}" == selectedDoctorText }
            selectedDoctor?.let {
                selectedDoctorId = it.doctorId
            } ?: run {
                Log.e(TAG, "Coudn't find doctor on the list")
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            photoUri = data.data
            Log.d("onActivityResult", data.data.toString())
            addPhotoImageView.setImageURI(photoUri)
            photoUri = Uri.parse(data.data.toString())
        }
    }

    private fun handleCheckboxes() {
        // Example: Handle checkbox selections and get user input from EditText
        val selectedSymptoms = mutableListOf<String>()

        checkBoxItching.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedSymptoms.add("Itching")
            } else {
                selectedSymptoms.remove("Itching")
            }
        }
    }

    private fun loadDoctors() {
        val doctorsCollection = firestore.collection("doctors")
        doctorsCollection.get()
            .addOnSuccessListener { doctorsResult ->
                val doctorsList = doctorsResult.toObjects(Doctor::class.java)
                val doctorNames = doctorsList.map { "${it.firstName} ${it.lastName}" }.toTypedArray()
                val docAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, doctorNames)
                autoDoc.setAdapter(docAdapter)
//                autoDoc.setOnItemClickListener { _, _, position, _ ->
//                    selectedDoctorId = doctorsList[position].doctorId
//                }
                setupAutoCompleteTextView()
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }



    private fun saveReport(photoUrl: String?) {
        // Pobierz UID aktualnie zalogowanego użytkownika
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Utwórz odwołanie do dokumentu użytkownika w Firestore
        val userRef = FirebaseFirestore.getInstance().collection("patients").document(currentUserUid!!)

        // Pobierz dane użytkownika z Firestore
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Konwertuj dane na obiekt użytkownika
                val user = documentSnapshot.toObject(Patient::class.java)

                // Sprawdź, czy udało się pobrać dane użytkownika
                user?.let {
                    val pesel = user.pesel


                    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                    dateFormat.timeZone =
                        TimeZone.getTimeZone("Europe/Warsaw") // Ustaw strefę czasową

                    val calendar = Calendar.getInstance()
                    val currentDate = calendar.time
                    val currentDateString = dateFormat.format(currentDate)
                    val doctorId = selectedDoctorId ?: return@addOnSuccessListener

                    val report = MedicalReport(
                        doctorId = doctorId,
                        patientPesel = pesel,
                        reportDate = currentDateString,
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
                        attachmentUrl = photoUri.toString()
                    )

                    val doctor = autoDoc.text.toString()


                    firestore.collection("report")
                        .add(report)
                        .addOnSuccessListener {
                                documentReference ->
                            val generatedReportId = documentReference.id

                            // Aktualizacja appointmentId z wygenerowanym ID
                            val updatedAppointment = report.copy(medicalReportId = generatedReportId)

                            // Ustawienie appointmentId w dokumencie Firestore
                            documentReference.set(updatedAppointment)
                                    Toast.makeText(
                                        this,
                                        "Report sent successfully.",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                    // Wyczyszczenie pól po udanym zapisaniu wizyty
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


                                }.addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to add report: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        }

                } else {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
        }
        }.addOnFailureListener { exception ->
            // Obsłuż błędy pobierania danych z Firestore
        }
    }
}