package com.example.dermapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
//import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import com.example.dermapp.database.Patient
import com.example.dermapp.startPatient.StartPatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity for editing patient's profile information.
 * Allows the patient to update their name, last name, email, and password securely.
 */
class EditProfilePatActivity : BaseActivity(), ConfirmationDialogFragment.ConfirmationDialogListener {

    // UI elements declaration
    private lateinit var backButton: ImageButton
    private lateinit var updateProfileButton: Button
//    private lateinit var profileImage: ImageView
//    private lateinit var profileImageButton: ImageButton

    /**
     * Called when the activity is starting.
     * Initializes UI elements, sets click listeners, and fetches current user's profile data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile_pat)

        // profileImageButton = findViewById(R.id.editProfileImagePat)

        // Set padding to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize back button and its click listener
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, StartPatActivity::class.java)
            startActivity(intent)
        }

        // Initialize update profile button and its click listener
        updateProfileButton = findViewById(R.id.buttonUpdateProfilePat)
        updateProfileButton.setOnClickListener {
            if (validateRegisterDetails()) {
                val confirmationDialog = ConfirmationDialogFragment()
                confirmationDialog.show(supportFragmentManager, "ConfirmationDialog")
            }
        }

        // Fetch current user's profile data and populate the UI fields
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

                }
            }
        }
    }

    /**
     * Callback function invoked when the user confirms password in the confirmation dialog.
     * Re-authenticates user and updates the profile upon successful re-authentication.
     */
    override fun onConfirmButtonClicked(password: String, dialog: DialogFragment) {
        dialog.dismiss()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
            currentUser.reauthenticate(credential)
                .addOnSuccessListener {
                    updateUser()
                    val intent = Intent(this, ProfilePatActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    showErrorSnackBar("Wrong password", true)
                }
        }
    }

    /**
     * Validates the input fields for updating profile details.
     * Checks for empty fields, valid name patterns, email format, and password criteria.
     */
    private fun validateRegisterDetails(): Boolean {
//        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val namePattern = "[a-zA-Z]+"

        val firstNameText: EditText = findViewById(R.id.editNamePat)
        val lastNameText: EditText = findViewById(R.id.editLastNamePat)
        // val emailText: EditText = findViewById(R.id.editMailPat)
        val passwordText: EditText = findViewById(R.id.editPasswordPat)
        val passwordRepeatText: EditText = findViewById(R.id.editPasswordRepeatPat)

        val firstName = firstNameText.text.toString().trim { it <= ' ' }
        val lastName = lastNameText.text.toString().trim { it <= ' ' }
        // val email = emailText.text.toString().trim { it <= ' ' }
        val password = passwordText.text.toString()
        val passwordRepeat = passwordRepeatText.text.toString()

        return when {
            TextUtils.isEmpty(firstName) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_name), true)
                false
            }

            !firstName.matches(namePattern.toRegex()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_name), true)
                false
            }

            TextUtils.isEmpty(lastName) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            !lastName.matches(namePattern.toRegex()) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_last_name), true)
                false
            }

//            TextUtils.isEmpty(email) -> {
//                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
//                false
//            }

//            !email.matches(emailPattern.toRegex()) -> {
//                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_email), true)
//                false
//            }

            (password != "" && password.length < 8) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_password), true)
                false
            }

            password != passwordRepeat -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_password_mismatch), true)
                false
            }

            else -> true
        }
    }

    /**
     * Updates the user's profile information in Firestore.
     * Updates first name, last name, email, and optionally the password.
     */
    private fun updateUser() {
        val firstNameText: EditText = findViewById(R.id.editNamePat)
        val lastNameText: EditText = findViewById(R.id.editLastNamePat)
//        val emailText: EditText = findViewById(R.id.editMailPat)
        val passwordText: EditText = findViewById(R.id.editPasswordPat)
        //val passwordRepeatText: EditText = findViewById(R.id.editPasswordRepeatPat)

        val firstName = firstNameText.text.toString().trim()
        val lastName = lastNameText.text.toString().trim()
//        val email = emailText.text.toString().trim()
        val password = passwordText.text.toString().trim()
        //val passwordRepeat = passwordRepeatText.text.toString().trim()

        val currentUser = FirebaseAuth.getInstance().currentUser

        // Update password if provided
        if (password != "") {
            if (currentUser != null) {
                currentUser.updatePassword(password)
            }
        }

        // Construct updates for Firestore document
        val currentUserUid = currentUser?.uid
        val userUpdates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName
            // Uncomment to include email in updates
            //"email" to email
        )

// Update Firestore document with new profile information
        if (currentUserUid != null) {
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







