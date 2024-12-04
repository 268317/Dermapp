package com.example.dermapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
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

    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        inputEmail = findViewById(R.id.editTextEmailAddress)
        inputPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.LogInButton)
        signUpButton = findViewById(R.id.SignUpButton)

        // Konfiguracja powiadomień
        configureNotificationChannel()

        loginButton.setOnClickListener { logInRegisteredUser() }
        signUpButton.setOnClickListener { goToSignIn() }
    }

    private fun configureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "messages_channel"
            val channelName = "Messages"
            val channelDescription = "Notifications for new messages"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun validateLoginDetails(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return when {
            TextUtils.isEmpty(inputEmail.text.toString().trim()) -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_email), true)
                false
            }
            !inputEmail.text.toString().matches(emailPattern.toRegex()) -> {
                showErrorSnackBar(getString(R.string.err_msg_invalid_email), true)
                false
            }
            TextUtils.isEmpty(inputPassword.text.toString().trim()) -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> true
        }
    }

    private fun logInRegisteredUser() {
        if (validateLoginDetails()) {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                        generateAndSaveFcmToken(userId)
                        showErrorSnackBar(getString(R.string.login_success), false)
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

    private fun generateAndSaveFcmToken(userId: String) {
        FirebaseMessaging.getInstance().deleteToken()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Previous FCM token deleted successfully.")
                    FirebaseMessaging.getInstance().token
                        .addOnCompleteListener { newTask ->
                            if (newTask.isSuccessful) {
                                val newFcmToken = newTask.result
                                saveFcmTokenToFirestore(userId, newFcmToken)
                            } else {
                                Log.e("FCM", "Failed to generate new FCM token: ${newTask.exception?.message}")
                            }
                        }
                } else {
                    Log.e("FCM", "Failed to delete FCM token: ${task.exception?.message}")
                }
            }
    }

    private fun saveFcmTokenToFirestore(userId: String, fcmToken: String) {
        firestore.collection("users").document(userId)
            .update("fcmToken", fcmToken)
            .addOnSuccessListener {
                Log.d("FCM", "FCM token updated for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error updating FCM token: ${e.message}")
            }
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
                                    showErrorSnackBar(getString(R.string.user_not_found), true)
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    showErrorSnackBar(getString(R.string.error_fetching_user), true)
                    Log.e("MainActivity", "Error fetching user data: ${e.message}")
                }
        }
    }

    private fun setUserOnlineState(userId: String, isOnline: Boolean) {
        firestore.collection("users").document(userId)
            .update("isOnline", isOnline)
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error updating online state: ${e.message}")
            }
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
