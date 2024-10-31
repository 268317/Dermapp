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
import com.bumptech.glide.request.RequestOptions
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

class EditProfileDocActivity : BaseActivity(), ConfirmationDialogFragment.ConfirmationDialogListener {

    private lateinit var backButton: ImageButton
    private lateinit var updateProfileButton: Button
    private lateinit var updateProfileImage: ImageButton

    companion object {
        const val GALLERY_REQUEST_CODE = 1001
        const val CAMERA_REQUEST_CODE = 1002
        const val REQUEST_IMAGE_CAPTURE = CAMERA_REQUEST_CODE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile_doc)

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
                    1 -> openCamera()
                }
            }
            builder.show()
        }

        val header = findViewById<LinearLayout>(R.id.backHeader)
        backButton = header.findViewById(R.id.arrowButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, ProfileDocActivity::class.java))
        }

        updateProfileButton = findViewById(R.id.buttonUpdateProfileDoc)
        updateProfileButton.setOnClickListener {
            if (validateRegisterDetails()) {
                val confirmationDialog = ConfirmationDialogFragment()
                confirmationDialog.show(supportFragmentManager, "ConfirmationDialog")
            }
        }

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseFirestore.getInstance().collection("doctors").document(currentUserUid!!)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(Doctor::class.java)
                user?.let {
                    findViewById<EditText>(R.id.editNameDoc).setText(user.firstName)
                    findViewById<EditText>(R.id.editLastNameDoc).setText(user.lastName)
                    loadProfileImage(user.profilePhoto)
                }
            }
        }
    }

    override fun onConfirmButtonClicked(password: String, dialog: DialogFragment) {
        dialog.dismiss()
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val credential = EmailAuthProvider.getCredential(it.email!!, password)
            it.reauthenticate(credential).addOnSuccessListener {
                updateUser()
                startActivity(Intent(this, ProfileDocActivity::class.java))
            }.addOnFailureListener {
                showErrorSnackBar("Wrong password", true)
            }
        }
    }

    private fun validateRegisterDetails(): Boolean {
        val namePattern = "[a-zA-Z]+"

        val firstName = findViewById<EditText>(R.id.editNameDoc).text.toString().trim()
        val lastName = findViewById<EditText>(R.id.editLastNameDoc).text.toString().trim()
        val password = findViewById<EditText>(R.id.editPasswordDoc).text.toString()
        val passwordRepeat = findViewById<EditText>(R.id.editPasswordRepeatDoc).text.toString()

        return when {
            TextUtils.isEmpty(firstName) -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_name), true)
                false
            }
            !firstName.matches(namePattern.toRegex()) -> {
                showErrorSnackBar(getString(R.string.err_msg_invalid_name), true)
                false
            }
            TextUtils.isEmpty(lastName) -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_last_name), true)
                false
            }
            !lastName.matches(namePattern.toRegex()) -> {
                showErrorSnackBar(getString(R.string.err_msg_invalid_last_name), true)
                false
            }
            password.isNotEmpty() && password.length < 8 -> {
                showErrorSnackBar(getString(R.string.err_msg_invalid_password), true)
                false
            }
            password != passwordRepeat -> {
                showErrorSnackBar(getString(R.string.err_msg_password_mismatch), true)
                false
            }
            else -> true
        }
    }

    private fun updateUser() {
        val firstName = findViewById<EditText>(R.id.editNameDoc).text.toString().trim()
        val lastName = findViewById<EditText>(R.id.editLastNameDoc).text.toString().trim()
        val password = findViewById<EditText>(R.id.editPasswordDoc).text.toString().trim()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (password.isNotEmpty()) {
            currentUser?.updatePassword(password)?.addOnSuccessListener {
                updatePasswordInFirestoreCollections(currentUser.uid, password)
            }?.addOnFailureListener {
                showErrorSnackBar("Error updating password: ${it.message}", true)
            }
        }

        val userUpdates = hashMapOf<String, Any>("firstName" to firstName, "lastName" to lastName)

        currentUser?.let {
            FirebaseFirestore.getInstance().collection("doctors").document(it.uid)
                .update(userUpdates)
                .addOnSuccessListener {
                    showErrorSnackBar("Profile updated successfully.", false)
                }
                .addOnFailureListener { e ->
                    showErrorSnackBar("Error updating profile: ${e.message}", true)
                }
        }
    }

    private fun updatePasswordInFirestoreCollections(userId: String, newPassword: String) {
        val hashedPassword = hashPassword(newPassword)

        FirebaseFirestore.getInstance().collection("doctors").document(userId)
            .update("password", hashedPassword).addOnFailureListener {
                showErrorSnackBar("Error updating password in doctors collection: ${it.message}", true)
            }

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update("password", hashedPassword).addOnFailureListener {
                showErrorSnackBar("Error updating password in users collection: ${it.message}", true)
            }
    }

    private fun hashPassword(password: String): String {
        return password // Replace with actual hashing
    }

    private lateinit var photoFile: File

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            photoFile = createImageFile()
            val photoURI: Uri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply { photoFile = this }
    }

    private fun loadProfileImage(profilePhotoUrl: String?) {
        if (!profilePhotoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profilePhotoUrl)
                .apply(RequestOptions.circleCropTransform()) // Circular image
                .into(updateProfileImage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        updateProfileImage.setImageURI(it)
                        uploadImageToFirestore(it)
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    val photoURI = Uri.fromFile(photoFile)
                    updateProfileImage.setImageURI(photoURI)
                    uploadImageToFirestore(photoURI)
                }
            }
        }
    }

    private fun uploadImageToFirestore(imageUri: Uri) {
        val profileImageRef = FirebaseStorage.getInstance().reference
            .child("profile_images/${FirebaseAuth.getInstance().currentUser?.uid}.jpg")

        profileImageRef.putFile(imageUri)
            .addOnSuccessListener {
                profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                    FirebaseFirestore.getInstance().collection("doctors")
                        .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                        .update("profilePhoto", uri.toString())
                        .addOnSuccessListener {
                            showErrorSnackBar("Profile picture updated successfully.", false)
                        }
                }
            }
            .addOnFailureListener {
                showErrorSnackBar("Error uploading profile picture: ${it.message}", true)
            }
    }
}
