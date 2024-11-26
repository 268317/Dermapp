package com.example.dermapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.example.dermapp.database.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Calendar

/**
 * Activity for user sign-up process.
 */
class SignUpActivity : BaseActivity() {
    private lateinit var signUpTextView: TextView
    private lateinit var buttonSignUp: Button
    private lateinit var radioButtonPatient: RadioButton
    private lateinit var radioButtonDoctor: RadioButton
    private lateinit var logInTextButton: TextView
    private lateinit var textName: EditText
    private lateinit var textLastName: EditText
    private lateinit var textEmail: EditText
    private lateinit var editTextTextPassword: EditText
    private lateinit var editTextTextPassword2: EditText
    private lateinit var editTextDateOfBirth: EditText
    private lateinit var doctorIdEditText: EditText
    private lateinit var peselEditText: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize UI elements
        signUpTextView = findViewById(R.id.SignUp_textView)
        buttonSignUp = findViewById(R.id.buttonSignUp)
        logInTextButton = findViewById(R.id.logInTextButton)
        textName = findViewById(R.id.TextName)
        textLastName = findViewById(R.id.TextLastName)
        textEmail = findViewById(R.id.TextEmail)
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth)
        editTextTextPassword = findViewById(R.id.editTextTextPassword)
        editTextTextPassword2 = findViewById(R.id.editTextTextPassword2)
        radioButtonPatient = findViewById(R.id.radioButton2)
        radioButtonDoctor = findViewById(R.id.radioButton1)
        doctorIdEditText = findViewById(R.id.textDoctorId)
        peselEditText = findViewById(R.id.textPesel)

        // Set click listeners
        buttonSignUp.setOnClickListener { registerUser() }
        logInTextButton.setOnClickListener { goToLogin() }
        editTextDateOfBirth.setOnClickListener { openCalendar() }

        radioButtonPatient.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                peselEditText.visibility = View.VISIBLE
                doctorIdEditText.visibility = View.GONE
            }
        }

        radioButtonDoctor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                doctorIdEditText.visibility = View.VISIBLE
                peselEditText.visibility = View.GONE
            }
        }
    }

    /**
     * Validates user input during registration.
     */
    private fun validateRegisterDetails(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val peselPattern = "\\d{11}"
        val namePattern = "[a-zA-Z]+"

        return when {
            TextUtils.isEmpty(textName.text.toString().trim()) -> {
                showErrorSnackBar("Please enter your name.", true)
                false
            }
            !textName.text.toString().trim().matches(namePattern.toRegex()) -> {
                showErrorSnackBar("Invalid name format.", true)
                false
            }
            TextUtils.isEmpty(textLastName.text.toString().trim()) -> {
                showErrorSnackBar("Please enter your last name.", true)
                false
            }
            !textLastName.text.toString().trim().matches(namePattern.toRegex()) -> {
                showErrorSnackBar("Invalid last name format.", true)
                false
            }
            TextUtils.isEmpty(textEmail.text.toString().trim()) -> {
                showErrorSnackBar("Please enter your email.", true)
                false
            }
            !textEmail.text.toString().trim().matches(emailPattern.toRegex()) -> {
                showErrorSnackBar("Invalid email format.", true)
                false
            }
            TextUtils.isEmpty(editTextTextPassword.text.toString().trim()) -> {
                showErrorSnackBar("Please enter your password.", true)
                false
            }
            editTextTextPassword.text.toString().trim().length < 8 -> {
                showErrorSnackBar("Password must be at least 8 characters.", true)
                false
            }
            editTextTextPassword.text.toString().trim() != editTextTextPassword2.text.toString().trim() -> {
                showErrorSnackBar("Passwords do not match.", true)
                false
            }
            radioButtonPatient.isChecked && !peselEditText.text.toString().trim().matches(peselPattern.toRegex()) -> {
                showErrorSnackBar("Invalid PESEL format.", true)
                false
            }
            else -> true
        }
    }

    /**
     * Registers the user in Firebase authentication and Firestore database.
     */
    private fun registerUser() {
        if (validateRegisterDetails()) {
            val email = textEmail.text.toString().trim()
            val password = editTextTextPassword.text.toString().trim()
            val firstName = textName.text.toString().trim()
            val lastName = textLastName.text.toString().trim()
            val birthday = editTextDateOfBirth.text.toString().trim()
            val isPatient = radioButtonPatient.isChecked
            val pesel = peselEditText.text.toString().trim()
            val doctorId = doctorIdEditText.text.toString().trim()
            val role = if (isPatient) "Patient" else "Doctor"

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener

                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val token = tokenTask.result

                                val user = AppUser(
                                    appUserId = userId,
                                    email = email,
                                    password = password,
                                    firstName = firstName,
                                    lastName = lastName,
                                    birthDate = birthday,
                                    role = role,
                                    fcmToken = token
                                )

                                FirebaseFirestore.getInstance().collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener {
                                        showErrorSnackBar("Registration successful.", false)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        showErrorSnackBar(e.message.toString(), true)
                                    }
                            }
                        }
                    } else {
                        showErrorSnackBar(task.exception?.message.toString(), true)
                    }
                }
        }
    }

    /**
     * Opens a calendar dialog to select the date of birth.
     */
    @SuppressLint("SetTextI18n")
    private fun openCalendar() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val maxDateCalendar = Calendar.getInstance()
        maxDateCalendar.set(Calendar.YEAR, year - 18)

        val datePickerDialog =
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDay = String.format("%02d", selectedDay)
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                editTextDateOfBirth.setText("$formattedDay-$formattedMonth-$selectedYear")
            }, year, month, dayOfMonth)

        datePickerDialog.datePicker.maxDate = maxDateCalendar.timeInMillis
        datePickerDialog.show()
    }

    /**
     * Navigates to the login screen.
     */
    private fun goToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
