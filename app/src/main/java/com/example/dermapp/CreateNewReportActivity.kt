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
import com.example.dermapp.database.Doctor
import com.example.dermapp.database.MedicalReport
import com.example.dermapp.database.Patient
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreateNewReportActivity : AppCompatActivity() {

    // Deklaracje pól UI i inne zmienne
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_report)

        // Inicjalizacja UI i inne operacje
        initializeUI()

        // Przycisk "Send Report"
        val sendReportButton = findViewById<Button>(R.id.buttonUpdateProfilePat)
        sendReportButton.setOnClickListener {
            // Przesyłanie zdjęcia do Firebase Storage
            uploadPhotoToFirebaseStorage()
        }

        // Obsługa kliknięcia na ImageView do dodawania zdjęcia
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

    private fun initializeUI() {
        // Inicjalizacja pól UI
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

        // Przycisk back
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        // Inicjalizacja AutoCompleteTextView dla lekarzy
        setupAutoCompleteTextView()

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
        // Otwarcie galerii zdjęć
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            photoUri = data.data
            addPhotoImageView.setImageURI(photoUri)
            //photoUri = Uri.parse(data.data.toString())
        }
    }

    private fun uploadPhotoToFirebaseStorage() {
        // Sprawdzenie czy URI zdjęcia jest null
        photoUri?.let { uri ->
            // Pobranie referencji do Firebase Storage
            val storageRef = FirebaseStorage.getInstance().reference
            // Utworzenie nazwy pliku dla zdjęcia w Storage
            val photoRef = storageRef.child("reports/${UUID.randomUUID()}")
            // Przesłanie zdjęcia do Firebase Storage
            val uploadTask = photoRef.putFile(uri)
            // Obsługa zdarzenia po przesłaniu zdjęcia
            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Pobranie URL przesłanego zdjęcia
                    photoRef.downloadUrl.addOnSuccessListener { url ->
                        // Po udanym przesłaniu zdjęcia, zapisz raport w Firestore
                        saveReport(url.toString())

                    }
                } else {
                    // Obsługa błędów przy przesyłaniu zdjęcia
                    Toast.makeText(this, "Failed to upload photo: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(this, "Please select a photo to upload", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveReport(photoUrl: String) {
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
                    dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw") // Ustaw strefę czasową

                    val calendar = Calendar.getInstance()
                    val currentDate = calendar.time
                    val currentDateString = dateFormat.format(currentDate)
                    val doctorId = selectedDoctorId ?: return@addOnSuccessListener

                    // Utwórz obiekt raportu medycznego
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

                    // Zapisz raport w Firestore
                    firestore.collection("report")
                        .add(report)
                        .addOnSuccessListener { documentReference ->
                            val generatedReportId = documentReference.id
                            // Aktualizacja appointmentId z wygenerowanym ID
                            val updatedReport = report.copy(medicalReportId = generatedReportId)
                            // Ustawienie appointmentId w dokumencie Firestore
                            documentReference.set(updatedReport)
                            Toast.makeText(
                                this,
                                "Report sent successfully.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Wyczyszczenie pól po udanym zapisaniu raportu
                            clearFields()
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
            exception.printStackTrace()
        }
    }

    private fun clearFields() {
        // Wyczyszczenie pól po wysłaniu raportu
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

    private fun loadDoctors() {
        // Pobranie listy lekarzy z Firestore
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
}