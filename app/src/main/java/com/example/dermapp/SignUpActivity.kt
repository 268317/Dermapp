package com.example.dermapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.example.dermapp.database.Doctor
import com.example.dermapp.database.Patient
import com.example.dermapp.database.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class SignUpActivity : BaseActivity() {
    private lateinit var signUpTextView: TextView
    private lateinit var buttonSignUp: Button
    private lateinit var radioButtonDoctor: RadioButton
    private lateinit var radioButtonPatient: RadioButton
    private lateinit var logInTextButton: TextView
    private lateinit var appnameTextview: TextView
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

        signUpTextView = findViewById(R.id.SignUp_textView)
        buttonSignUp = findViewById(R.id.buttonSignUp)
        logInTextButton = findViewById(R.id.logInTextButton)
        appnameTextview = findViewById(R.id.AppName_textView)
        textName = findViewById(R.id.TextName)
        textLastName = findViewById(R.id.TextLastName)
        textEmail = findViewById(R.id.TextEmail)
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth)
        editTextTextPassword = findViewById(R.id.editTextTextPassword)
        editTextTextPassword2 = findViewById(R.id.editTextTextPassword2)
        radioButtonDoctor = findViewById(R.id.radioButton1)
        radioButtonPatient = findViewById(R.id.radioButton2)
        doctorIdEditText = findViewById(R.id.textDoctorId)
        peselEditText = findViewById(R.id.textPesel)

        buttonSignUp.setOnClickListener {
            registerUser()
        }

        logInTextButton.setOnClickListener {
            goToLogin()
        }

        editTextDateOfBirth.setOnClickListener {
            openCalendar()
        }

        radioButtonDoctor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                doctorIdEditText.visibility = View.VISIBLE
                peselEditText.visibility = View.GONE
            }
        }

        radioButtonPatient.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                peselEditText.visibility = View.VISIBLE
                doctorIdEditText.visibility = View.GONE
            }
        }
    }



    private fun validateRegisterDetails(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val peselPattern = "\\d{11}"
        val namePattern = "[a-zA-Z]+"

        return when {
            TextUtils.isEmpty(textName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_name), true)
                false
            }

            !textName.text.toString().trim { it <= ' ' }.matches(namePattern.toRegex()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_name), true)
                false
            }

            TextUtils.isEmpty(textLastName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            !textLastName.text.toString().trim { it <= ' ' }.matches(namePattern.toRegex()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_last_name), true)
                false
            }

            TextUtils.isEmpty(textEmail.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            !textEmail.text.toString().trim { it <= ' ' }.matches(emailPattern.toRegex()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_email), true)
                false
            }

            TextUtils.isEmpty(textName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_name), true)
                false
            }

            TextUtils.isEmpty(textLastName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            TextUtils.isEmpty(editTextTextPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            editTextTextPassword.text.toString().trim { it <= ' ' }.length < 8  -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_password), true)
                false
            }

            TextUtils.isEmpty(editTextTextPassword2.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_reppassword), true)
                false
            }

            editTextTextPassword.text.toString()
                .trim { it <= ' ' } != editTextTextPassword2.text.toString()
                .trim { it <= ' ' } -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_password_mismatch), true)
                false
            }

            radioButtonPatient.isChecked && !peselEditText.text.toString().trim { it <= ' ' }.matches(peselPattern.toRegex())
                    || radioButtonPatient.isChecked && peselEditText.text.toString().trim { it <= ' ' }.length != 11 -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_pesel), true)
                false
            }

            else -> true
        }
    }


    private fun goToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun openCalendar() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDay = String.format("%02d", selectedDay)
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                editTextDateOfBirth.setText("$formattedDay-$formattedMonth-$selectedYear")
            }, year, month, dayOfMonth)

        datePickerDialog.show()
    }

    private fun registerUser() {
        if (validateRegisterDetails()) {
            val email: String = textEmail.text.toString().trim()
            val password: String = editTextTextPassword.text.toString().trim()
            val firstName: String = textName.text.toString().trim()
            val lastName: String = textLastName.text.toString().trim()
            val birthday = editTextDateOfBirth.text.toString()
            val isPatient = radioButtonPatient.isChecked
            val isDoctor = radioButtonDoctor.isChecked
            val pesel = peselEditText.text.toString().trim()
            val doctorId = doctorIdEditText.text.toString().trim()

            val role: String = when {
                isPatient && !isDoctor -> "Patient"
                !isPatient && isDoctor -> "Doctor"
                else -> {
                    Toast.makeText(this, "Please select a role.", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            val user = AppUser(email, password, firstName, lastName, birthday, role)

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                        val uid: String = firebaseUser?.uid ?: ""

                        user.userId = uid // Przypisz UID użytkownika do obiektu użytkownika
                        user.firstName = firstName
                        user.lastName = lastName
                        user.email = email
                        user.birthDate = birthday
                        user.password = password
                        user.role = role

                        // Zapisz użytkownika do Firestore
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                            .set(user)
                            .addOnSuccessListener {
                                showErrorSnackBar(
                                    "You are registered successfully. Your user id is $uid",
                                    false
                                )
                                finish()
                            }
                            .addOnFailureListener { e ->
                                showErrorSnackBar(e.message.toString(), true)
                            }

                        if (isPatient) {
                            val patient = Patient(email, password, firstName, lastName, birthday, role, pesel)
                            patient.pesel = pesel
                            user.userId = uid
                            user.lastName = lastName
                            user.email = email
                            user.birthDate = birthday
                            user.password = password
                            user.role = role
                            FirebaseFirestore.getInstance().collection("patients").document(uid)
                                .set(patient)
                                .addOnSuccessListener {
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    showErrorSnackBar(e.message.toString(), true)
                                }
                        } else if (isDoctor) {
                            val doctor = Doctor(email, password, firstName, lastName, birthday, role, doctorId)
                            doctor.doctorId = doctorId
                            user.userId = uid // Przypisz UID użytkownika do obiektu użytkownika
                            user.firstName = firstName
                            user.lastName = lastName
                            user.email = email
                            user.birthDate = birthday
                            user.password = password
                            user.role = role
                            FirebaseFirestore.getInstance().collection("doctors").document(uid)
                                .set(doctor)
                                .addOnSuccessListener {
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    showErrorSnackBar(e.message.toString(), true)
                                }
                        }

                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }
    fun userRegistrationSuccess() {
        Toast.makeText(
            this@SignUpActivity,
            resources.getString(R.string.register_success),
            Toast.LENGTH_LONG
        ).show()
    }

}