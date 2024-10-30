package com.example.dermapp

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.dermapp.database.Doctor
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity for editing doctor's profile information.
 * Allows the doctor to update their name, last name, and password securely.
 */
class EditProfileDocActivity : BaseActivity(), ConfirmationDialogFragment.ConfirmationDialogListener {

    // UI elements declaration
    private lateinit var backButton: ImageButton
    private lateinit var updateProfileButton: Button
    private lateinit var updateProfileImage: ImageButton

    companion object {
        const val GALLERY_REQUEST_CODE = 1001
        const val CAMERA_REQUEST_CODE = 1002
        const val REQUEST_IMAGE_CAPTURE = CAMERA_REQUEST_CODE
    }

    /**
     * Called when the activity is starting.
     * Initializes UI elements, sets click listeners, and fetches current user's profile data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile_doc)

        // Set padding to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        updateProfileImage = findViewById(R.id.editProfileImageDoc)
        updateProfileImage.setOnClickListener {
            val options = arrayOf("Open gallery", "New photo")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Update profile picture")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val galleryIntent = Intent(Intent.ACTION_PICK)
                        galleryIntent.type = "image/*"
                        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
                    }
                    1 -> {
                        openCamera()
                    }
                }
            }
            builder.show()
        }


        // Initialize back button and its click listener
        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            val intent = Intent(this, ProfileDocActivity::class.java)
            startActivity(intent)
        }

        // Initialize update profile button and its click listener
        updateProfileButton = findViewById(R.id.buttonUpdateProfileDoc)
        updateProfileButton.setOnClickListener {
            if (validateRegisterDetails()) {
                val confirmationDialog = ConfirmationDialogFragment()
                confirmationDialog.show(supportFragmentManager, "ConfirmationDialog")
            }
        }

        // Fetch current user's profile data and populate the UI fields
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseFirestore.getInstance().collection("doctors").document(currentUserUid!!)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(Doctor::class.java)
                user?.let {
                    val firstNameText: EditText = findViewById(R.id.editNameDoc)
                    firstNameText.setText(user.firstName)

                    val lastNameText: EditText = findViewById(R.id.editLastNameDoc)
                    lastNameText.setText(user.lastName)

                    loadProfileImage(user.profilePhoto)
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
                    val intent = Intent(this, ProfileDocActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    showErrorSnackBar("Wrong password", true)
                }
        }
    }

    /**
     * Validates the input fields for updating profile details.
     * Checks for empty fields, valid name patterns, and password criteria.
     */
    private fun validateRegisterDetails(): Boolean {
        val namePattern = "[a-zA-Z]+"

        val firstNameText: EditText = findViewById(R.id.editNameDoc)
        val lastNameText: EditText = findViewById(R.id.editLastNameDoc)
        val passwordText: EditText = findViewById(R.id.editPasswordDoc)
        val passwordRepeatText: EditText = findViewById(R.id.editPasswordRepeatDoc)

        val firstName = firstNameText.text.toString().trim { it <= ' ' }
        val lastName = lastNameText.text.toString().trim { it <= ' ' }
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
     * Updates first name, last name, and optionally the password.
     */
    /**
     * Updates the user's profile information in Firestore.
     * Updates first name, last name, and optionally the password.
     */
    private fun updateUser() {
        val firstNameText: EditText = findViewById(R.id.editNameDoc)
        val lastNameText: EditText = findViewById(R.id.editLastNameDoc)
        val passwordText: EditText = findViewById(R.id.editPasswordDoc)

        val firstName = firstNameText.text.toString().trim()
        val lastName = lastNameText.text.toString().trim()
        val password = passwordText.text.toString().trim()
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Update password if provided
        if (password != "" && currentUser != null) {
            currentUser.updatePassword(password)
                .addOnSuccessListener {
                    // Password update was successful, now update in Firestore collections
                    updatePasswordInFirestoreCollections(currentUser.uid, password)
                }
                .addOnFailureListener { e ->
                    showErrorSnackBar("Error updating password: ${e.message}", true)
                }
        }

        // Construct updates for Firestore document
        val userUpdates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName
        )

        // Update Firestore document with new profile information
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("doctors").document(currentUser.uid)
                .update(userUpdates)
                .addOnSuccessListener {
                    showErrorSnackBar("Profile updated successfully.", false)
                }
                .addOnFailureListener { e ->
                    showErrorSnackBar("Error updating profile: ${e.message}", true)
                }
        }
    }

    /**
     * Updates the password in the 'doctors' and 'users' Firestore collections.
     */
    private fun updatePasswordInFirestoreCollections(userId: String, newPassword: String) {
        // Hash the password before saving (if necessary).
        val hashedPassword = hashPassword(newPassword)

        // Update the 'doctors' collection
        FirebaseFirestore.getInstance().collection("doctors").document(userId)
            .update("password", hashedPassword)
            .addOnSuccessListener {
                // Optionally, show a success message
                showErrorSnackBar("Password updated in doctors collection.", false)
            }
            .addOnFailureListener { e ->
                showErrorSnackBar("Error updating password in doctors collection: ${e.message}", true)
            }

        // Update the 'users' collection (if necessary)
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update("password", hashedPassword)
            .addOnSuccessListener {
                // Optionally, show a success message
                showErrorSnackBar("Password updated in users collection.", false)
            }
            .addOnFailureListener { e ->
                showErrorSnackBar("Error updating password in users collection: ${e.message}", true)
            }
    }

    /**
     * Hashes the password before storing it.
     * You can use any preferred hashing mechanism here.
     */
    private fun hashPassword(password: String): String {
        // Implement password hashing logic (e.g., using SHA-256 or another hashing algorithm)
        return password // For now, just return the password directly (NOTE: Replace with actual hashing)
    }

    private lateinit var photoFile: File

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            photoFile = createImageFile()
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                photoFile
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            photoFile = this
        }
    }

    private fun loadProfileImage(profilePhotoUrl: String?) {
        if (!profilePhotoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profilePhotoUrl)
                .into(updateProfileImage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                EditProfilePatActivity.GALLERY_REQUEST_CODE -> {
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        updateProfileImage.setImageURI(it)
                        uploadImageToFirestore(it)
                    }
                }
                EditProfilePatActivity.CAMERA_REQUEST_CODE -> {
                    val photoURI = Uri.fromFile(photoFile)
                    updateProfileImage.setImageURI(photoURI)
                    uploadImageToFirestore(photoURI)
                }
            }
        }
    }

    private fun uploadImageToFirestore(imageUri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference
        val profileImagesRef = storageReference.child("profile_images/${System.currentTimeMillis()}.jpg")

        profileImagesRef.putFile(imageUri)
            .addOnSuccessListener {
                profileImagesRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
                    val userRef = FirebaseFirestore.getInstance().collection("doctors").document(currentUserUid!!)
                    userRef.update("profilePhoto", downloadUri.toString())
                        .addOnSuccessListener {
                            showErrorSnackBar("Profile photo updated successfully", false)
                        }
                        .addOnFailureListener { e ->
                            showErrorSnackBar("Error saving photo URL: ${e.message}", true)
                        }
                }
            }
            .addOnFailureListener { e ->
                showErrorSnackBar("Error uploading image: ${e.message}", true)
            }
    }

}