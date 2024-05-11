package com.example.dermapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.example.dermapp.database.Doctor
import com.example.dermapp.database.FirestoreClass
import com.example.dermapp.database.Patient
import com.example.dermapp.database.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

        buttonSignUp.setOnClickListener {
            registerUser()
        }

        logInTextButton.setOnClickListener {
            goToLogin()
        }

        editTextDateOfBirth.setOnClickListener {
            openCalendar()
        }
    }

    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(textEmail.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
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
            val repeatPassword: String = editTextTextPassword2.text.toString().trim()
            val name: String = textName.text.toString().trim()
            val lastName: String = textLastName.text.toString().trim()
            val birthday = editTextDateOfBirth.text.toString()

            val isPatient = radioButtonPatient.isChecked
            val isDoctor = radioButtonDoctor.isChecked
            val role: String = if (isPatient && !isDoctor) {
                "Patient"
            } else if (!isPatient && isDoctor) {
                "Doctor"
            } else {
                Toast.makeText(this, "Please select a role.", Toast.LENGTH_SHORT).show()
                return
            }

            if (email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()
                && name.isNotEmpty() && lastName.isNotEmpty() && birthday.isNotEmpty()
            ) {
                if (password.length < 8 || !password.matches(Regex(".*\\d.*"))) {
                    editTextTextPassword.error =
                        "Password must be at least 8 characters long and contain at least one digit."
                    return
                }
                if (password != repeatPassword) {
                    editTextTextPassword2.error = "Passwords do not match."
                    return
                }

                if (role == "Patient") {
                    Patient(name, lastName, birthday)
                } else {
                    Doctor(name, lastName, birthday)
                }

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            showErrorSnackBar(
                                "You are registered successfully. Your user id is ${firebaseUser.uid}",
                                false
                            )

                            val user = User(
                                "Testowe ID",
                                name,
                                lastName,
                                true,
                                email,
                                role,
                            )
                            FirestoreClass().registerUser(this@SignUpActivity, user)

                            FirebaseAuth.getInstance().signOut()
                            finish()

                        } else {
                            showErrorSnackBar(task.exception!!.message.toString(), true)
                        }
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