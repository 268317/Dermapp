package com.example.dermapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*

class CreateNewReportActivity : AppCompatActivity() {

    // Declare UI elements
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_report)

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

        // Handle checkbox states or other interactions
        handleCheckboxes()

        // Handle adding photo (click listener for ImageView)
        addPhotoImageView.setOnClickListener {
            // Implement photo adding logic here
            Toast.makeText(this, "Add photo clicked", Toast.LENGTH_SHORT).show()
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
}
