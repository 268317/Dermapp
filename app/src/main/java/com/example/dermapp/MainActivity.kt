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
import com.example.dermapp.startDoctor.StartDocActivity
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity() {

    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var loginButton: Button? = null
    private var signUpButton: Button? = null

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        inputEmail = findViewById(R.id.editTextEmailAddress)
        inputPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.LogInButton)
        signUpButton = findViewById(R.id.SignUpButton)

        loginButton?.setOnClickListener { logInRegisteredUser() }
        signUpButton?.setOnClickListener { goToSignIn() }
    }

    private fun validateLoginDetails(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return when {
            TextUtils.isEmpty(inputEmail?.text.toString().trim()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            !inputEmail?.text.toString().matches(emailPattern.toRegex()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_email), true)
                false
            }
            TextUtils.isEmpty(inputPassword?.text.toString().trim()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> true
        }
    }

    private fun logInRegisteredUser() {
        if (validateLoginDetails()) {
            val email = inputEmail?.text.toString().trim()
            val password = inputPassword?.text.toString().trim()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                        FirebaseMessaging.getInstance().deleteToken()
                            .addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    FirebaseMessaging.getInstance().token
                                        .addOnCompleteListener { tokenTask ->
                                            if (tokenTask.isSuccessful) {
                                                updateFcmToken(userId, tokenTask.result)
                                            }
                                        }
                                }
                            }
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

    private fun updateFcmToken(userId: String, newToken: String) {
        firestore.collection("users").document(userId)
            .update("fcmToken", newToken)
            .addOnSuccessListener { Log.d("FCM", "Token updated for user: $userId") }
            .addOnFailureListener { e -> Log.e("FCM", "Error updating token: ${e.message}") }
    }

    private fun goToNextActivity() {
        val user = auth.currentUser
        val uid = user?.uid ?: ""
        if (user != null) {
            firestore.collection("patients").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        startActivity(Intent(this, StartPatActivity::class.java).apply {
                            putExtra("uID", uid)
                        })
                    } else {
                        firestore.collection("doctors").document(uid)
                            .get()
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    startActivity(Intent(this, StartDocActivity::class.java).apply {
                                        putExtra("uID", uid)
                                    })
                                } else {
                                    showErrorSnackBar("User not found in the system", true)
                                }
                            }
                    }
                }
        }
    }

    private fun setUserOnlineState(userId: String, isOnline: Boolean) {
        firestore.collection("users").document(userId)
            .update("isOnline", isOnline)
    }

    private fun goToSignIn() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    inner class AppLifecycleObserver(private val userId: String) : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onAppBackgrounded() {
            setUserOnlineState(userId, false)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onAppForegrounded() {
            setUserOnlineState(userId, true)
        }
    }
}
