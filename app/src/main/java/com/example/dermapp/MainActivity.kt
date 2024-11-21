package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.dermapp.messages.MessagesPatActivity
import com.example.dermapp.startDoctor.StartDocActivity
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Activity responsible for user login using Firebase Authentication.
 */
class MainActivity : BaseActivity() {

    // UI elements
    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var loginButton: Button? = null
    private var signUpButton: Button? = null

    /**
     * Called when the activity is starting.
     * Initializes UI elements and sets click listeners for login and sign up buttons.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Testowe połączenie z Firestore
        FirebaseFirestore.getInstance().collection("Test").add(mapOf("testKey" to "testValue"))
            .addOnSuccessListener {
                Log.d("FirebaseTest", "Połączenie z Firestore działa!")
            }
            .addOnFailureListener {
                Log.e("FirebaseTest", "Błąd połączenia: ${it.message}")
            }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM Token", "Token urządzenia: $token")
            } else {
                Log.e("FCM Token", "Błąd podczas pobierania tokenu", task.exception)
            }
        }

        // Initialize input fields and login button
        inputEmail = findViewById(R.id.editTextEmailAddress)
        inputPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.LogInButton)
        signUpButton = findViewById(R.id.SignUpButton)

        // Set click listeners for login and sign up buttons
        loginButton?.setOnClickListener{
            logInRegisteredUser()
        }

        signUpButton?.setOnClickListener{
            goToSignIn()
        }
    }

    /**
     * Validates the entered login details.
     * @return True if details are valid, false otherwise.
     */
    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(inputEmail?.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(inputPassword?.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            else -> {
                showErrorSnackBar("Your details are valid", false)
                true
            }
        }
    }

    /**
     * Logs in a registered user using Firebase Authentication.
     */
    private fun logInRegisteredUser() {
        if (validateLoginDetails()) {
            val email = inputEmail?.text.toString().trim { it<= ' '}
            val password = inputPassword?.text.toString().trim { it<= ' '}

            // Sign in with FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showErrorSnackBar("You are logged in successfully.", false)
                        goToNextActivity()
                        finish()
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    /**
     * Redirects to the appropriate activity after successful login and passes the user's UID.
     */
    private fun goToNextActivity() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: ""
        if (user != null) {
            FirebaseFirestore.getInstance().collection("patients").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val intent = Intent(this, StartPatActivity::class.java)
                        intent.putExtra("uID", uid)
                        startActivity(intent)
                    } else {
                        FirebaseFirestore.getInstance().collection("doctors").document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val intent = Intent(this, StartDocActivity::class.java)
                                    intent.putExtra("uID", uid)
                                    startActivity(intent)
                                } else {
                                    val intent = Intent(this, MessagesPatActivity::class.java)
                                    intent.putExtra("uID", uid)
                                    startActivity(intent)
                                }
                            }
                    }
                }
        }
    }

    /**
     * Redirects to the sign up activity.
     */
    private fun goToSignIn() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }
}