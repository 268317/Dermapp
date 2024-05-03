package com.example.dermapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var userNameInput: EditText
    private lateinit var userPasswordInput: EditText
    private lateinit var logInButton: Button
    private lateinit var signUpButton: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        userNameInput = findViewById(R.id.editTextEmailAddress)
        userPasswordInput = findViewById(R.id.editTextPassword)
        logInButton = findViewById(R.id.LogInButton)
        signUpButton = findViewById(R.id.SignUpButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        signUpButton.setOnClickListener {
            openSignUpActivity()
        }

        logInButton.setOnClickListener {
            logInActivity()
        }
    }
    private fun openSignUpActivity(){
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun logInActivity(){
        val email = userNameInput.text.toString()
        val password = userPasswordInput.text.toString()
        val intent = Intent(this, StartPatActivity::class.java)
        startActivity(intent)
    }
}