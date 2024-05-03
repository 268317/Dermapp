package com.example.dermapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SignUpActivity : AppCompatActivity() {
    private lateinit var SignUp_textView: TextView
    private lateinit var buttonSignUp: Button
    private lateinit var logInTextButton: TextView
    private lateinit var AppName_textView: TextView
    private lateinit var TextName: EditText
    private lateinit var TextLastName: EditText
    private lateinit var TextEmail: EditText
    private lateinit var editTextTextPassword: EditText
    private lateinit var editTextTextPassword2: EditText
    private lateinit var editTextDateOfBirth: EditText


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        SignUp_textView = findViewById(R.id.SignUp_textView)
        buttonSignUp = findViewById(R.id.buttonSignUp)
        logInTextButton = findViewById(R.id.LogInTextButton)
        AppName_textView = findViewById(R.id.AppName_textView)
        TextName = findViewById(R.id.TextName)
        TextLastName = findViewById(R.id.TextLastName)
        TextEmail = findViewById(R.id.TextEmail)
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth)
        editTextTextPassword = findViewById(R.id.editTextTextPassword)
        editTextTextPassword2 = findViewById(R.id.editTextTextPassword2)

        logInTextButton.setOnClickListener {
            logIn()
        }

        editTextDateOfBirth.setOnClickListener{
            openCalendar()
        }
    }

    private fun logIn(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun openCalendar() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDay = String.format("%02d", selectedDay)
            val formattedMonth = String.format("%02d", selectedMonth + 1)
            editTextDateOfBirth.setText("$formattedDay-$formattedMonth-$selectedYear")
        }, year, month, dayOfMonth)

        datePickerDialog.show()
    }
}
