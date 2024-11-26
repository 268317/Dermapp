package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
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

    // Firebase references
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize input fields and login button
        inputEmail = findViewById(R.id.editTextEmailAddress)
        inputPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.LogInButton)
        signUpButton = findViewById(R.id.SignUpButton)

        // Set click listeners for login and sign up buttons
        loginButton?.setOnClickListener {
            logInRegisteredUser()
        }

        signUpButton?.setOnClickListener {
            goToSignIn()
        }
    }

    /**
     * Validates the entered login details.
     * @return True if details are valid, false otherwise.
     */
    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(inputEmail?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(inputPassword?.text.toString().trim { it <= ' ' }) -> {
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
            val email = inputEmail?.text.toString().trim { it <= ' ' }
            val password = inputPassword?.text.toString().trim { it <= ' ' }

            // Sign in with FirebaseAuth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        // Delete the existing token to force generation of a new one
                        FirebaseMessaging.getInstance().deleteToken()
                            .addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    // Generate a new token
                                    FirebaseMessaging.getInstance().token
                                        .addOnCompleteListener { tokenTask ->
                                            if (tokenTask.isSuccessful) {
                                                val newToken = tokenTask.result
                                                updateFcmToken(userId, newToken)
                                            } else {
                                                Log.e("FCM", "Failed to generate new token: ${tokenTask.exception?.message}")
                                            }
                                        }
                                } else {
                                    Log.e("FCM", "Failed to delete token: ${deleteTask.exception?.message}")
                                }
                            }

                        // Proceed with login workflow
                        showErrorSnackBar("You are logged in successfully.", false)
                        setUserOnlineState(userId, true)
                        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(userId))
                        goToNextActivity()
                        finish()
                    } else {
                        showErrorSnackBar(task.exception?.message.toString(), true)
                    }
                }
        }
    }



    /**
     * Updates the FCM token for the logged-in user in Firestore.
     * @param userId The user's unique identifier.
     */
    private fun updateFcmToken(userId: String, newToken: String) {
        firestore.collection("users").document(userId)
            .update("fcmToken", newToken)
            .addOnSuccessListener {
                Log.d("FCM", "New token updated for user: $userId, token: $newToken")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error updating new token: ${e.message}")
            }
    }


    /**
     * Redirects to the appropriate activity after successful login and passes the user's UID.
     */
    private fun goToNextActivity() {
        val user = auth.currentUser
        val uid = user?.uid ?: ""
        if (user != null) {
            firestore.collection("patients").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val intent = Intent(this, StartPatActivity::class.java)
                        intent.putExtra("uID", uid)
                        startActivity(intent)
                    } else {
                        firestore.collection("doctors").document(uid)
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

    /**
     * Sets the user's online state in Firestore.
     * @param userId The user's ID.
     * @param isOnline Boolean indicating whether the user is online.
     */
    private fun setUserOnlineState(userId: String, isOnline: Boolean) {
        firestore.collection("users").document(userId)
            .update("isOnline", isOnline)
            .addOnSuccessListener {
                Log.d("OnlineState", "isOnline ustawione na $isOnline dla użytkownika $userId")
            }
            .addOnFailureListener { e ->
                Log.e("OnlineState", "Błąd podczas ustawiania isOnline: ${e.message}")
            }
    }

    /**
     * Lifecycle observer to update user's online state.
     */
    inner class AppLifecycleObserver(private val userId: String) : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onEnterForeground() {
            setUserOnlineState(userId, true)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onEnterBackground() {
            setUserOnlineState(userId, false)
        }
    }
}
