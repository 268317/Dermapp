package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dermapp.database.AppUser
import com.example.dermapp.database.Patient
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class EditProfilePatActivity : BaseActivity(), ConfirmationDialogFragment.ConfirmationDialogListener {
    private lateinit var backButton: ImageButton
    private lateinit var updateProfileButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile_pat)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)

        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        updateProfileButton = findViewById(R.id.buttonUpdateProfilePat)
        updateProfileButton.setOnClickListener {
            val confirmationDialog = ConfirmationDialogFragment()
            confirmationDialog.show(supportFragmentManager, "ConfirmationDialog")
        }

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseFirestore.getInstance().collection("patients").document(currentUserUid!!)

        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {

                val user = documentSnapshot.toObject(Patient::class.java)

                user?.let {
                    val firstNameText: EditText = findViewById(R.id.editNamePat)
                    firstNameText.setText(user.firstName)

                    val lastNameText: EditText = findViewById(R.id.editLastNamePat)
                    lastNameText.setText(user.lastName)

                    val emailText: EditText = findViewById(R.id.editMailPat)
                    emailText.setText(user.email)
                }
            }
        }
    }

    override fun onConfirmButtonClicked(password: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
            currentUser.reauthenticate(credential)
                .addOnSuccessListener {
                    updateUser()
                }
                .addOnFailureListener { e ->
                    showErrorSnackBar("Wrong password", false)
                }
        }
        val intent = Intent(this, ProfilePatActivity::class.java)
        startActivity(intent)
    }

    private fun validateRegisterDetails(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val namePattern = "[a-zA-Z]+"

        val firstNameText: EditText = findViewById(R.id.editNamePat)
        val lastNameText: EditText = findViewById(R.id.editLastNamePat)
        val emailText: EditText = findViewById(R.id.editMailPat)

        return when {
            TextUtils.isEmpty(firstNameText.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_name), true)
                false
            }

            !firstNameText.text.toString().trim { it <= ' ' }.matches(namePattern.toRegex()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_name), true)
                false
            }

            TextUtils.isEmpty(lastNameText.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            !lastNameText.text.toString().trim { it <= ' ' }.matches(namePattern.toRegex()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_last_name), true)
                false
            }

            TextUtils.isEmpty(emailText.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            !emailText.text.toString().trim { it <= ' ' }.matches(emailPattern.toRegex()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_email), true)
                false
            }

            TextUtils.isEmpty(firstNameText.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_name), true)
                false
            }

            TextUtils.isEmpty(lastNameText.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            else -> true
        }
    }

    private fun updateUser() {
        if (validateRegisterDetails()) {
            val firstNameText: EditText = findViewById(R.id.editNamePat)
            val lastNameText: EditText = findViewById(R.id.editLastNamePat)
            val emailText: EditText = findViewById(R.id.editMailPat)
            val passwordText: EditText = findViewById(R.id.editPasswordPat)
            val passwordRepeatText: EditText = findViewById(R.id.editPasswordRepeatPat)

            val firstName = firstNameText.text.toString().trim()
            val lastName = lastNameText.text.toString().trim()
            val email = emailText.text.toString().trim()
            val password = passwordText.text.toString().trim()
            val passwordRepeat = passwordRepeatText.text.toString().trim()

            var isPasswordChanging = true

            if (password.isNotEmpty() && password != "Edit Password") {
                if (password.length < 8) {
                    isPasswordChanging = false
                    showErrorSnackBar(resources.getString(R.string.err_msg_invalid_password), true)
                    return
                }
                if (password != passwordRepeat) {
                    isPasswordChanging = false
                    showErrorSnackBar(resources.getString(R.string.err_msg_password_mismatch), true)
                    return
                }
                isPasswordChanging = true
            }

            val currentUser = FirebaseAuth.getInstance().currentUser

            currentUser?.updateEmail(email)?.addOnCompleteListener { emailUpdateTask ->
                    if (isPasswordChanging && emailUpdateTask.isSuccessful) {
                        val userUpdates = hashMapOf<String, Any>(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "email" to email,
                            "password" to password
                        )

                        // AUTH UPDATE!!!!

                        val currentUserUid = currentUser.uid

                        FirebaseFirestore.getInstance().collection("patients").document(currentUserUid)
                            .update(userUpdates)
                            .addOnSuccessListener {
                                showErrorSnackBar("Profile updated successfully.", false)
                            }
                            .addOnFailureListener { e ->
                                showErrorSnackBar("Error updating profile: ${e.message}", true)
                            }
                    } else {
                        val userUpdates = hashMapOf<String, Any>(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "email" to email,
                        )

                        // AUTH UPDATE!!!!

                        val currentUserUid = currentUser.uid

                        FirebaseFirestore.getInstance().collection("patients").document(currentUserUid)
                            .update(userUpdates)
                            .addOnSuccessListener {
                                showErrorSnackBar("Profile updated successfully.", false)
                            }
                            .addOnFailureListener { e ->
                                showErrorSnackBar("Error updating profile: ${e.message}", true)
                            }
                    }

            }
        }
    }

}
