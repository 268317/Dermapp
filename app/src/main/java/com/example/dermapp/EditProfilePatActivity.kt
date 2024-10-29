package com.example.dermapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
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
import com.example.dermapp.database.Patient
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditProfilePatActivity : BaseActivity(), ConfirmationDialogFragment.ConfirmationDialogListener {

    private lateinit var backButton: ImageButton
    private lateinit var updateProfileButton: Button
    private lateinit var imageButtonProfile: ImageButton

    companion object {
        private const val GALLERY_REQUEST_CODE = 1001
        private const val CAMERA_REQUEST_CODE = 1002
        private const val REQUEST_IMAGE_CAPTURE = CAMERA_REQUEST_CODE
    }

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
            val intent = Intent(this, ProfilePatActivity::class.java)
            startActivity(intent)
        }

        updateProfileButton = findViewById(R.id.buttonUpdateProfilePat)
        updateProfileButton.setOnClickListener {
            if (validateRegisterDetails()) {
                val confirmationDialog = ConfirmationDialogFragment()
                confirmationDialog.show(supportFragmentManager, "ConfirmationDialog")
            }
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

                    loadProfileImage(user.profilePhoto)
                }
            }
        }

        imageButtonProfile = findViewById(R.id.editProfileImagePat)
        imageButtonProfile.setOnClickListener {
            val options = arrayOf("Wybierz z galerii", "Otwórz aparat")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Aktualizuj zdjęcie profilowe")
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
                .into(imageButtonProfile)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        imageButtonProfile.setImageURI(it)
                        uploadImageToFirestore(it)
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    val photoURI = Uri.fromFile(photoFile)
                    imageButtonProfile.setImageURI(photoURI)
                    uploadImageToFirestore(photoURI)
                }
            }
        }
    }

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

    private fun validateRegisterDetails(): Boolean {
        val namePattern = "[a-zA-Z]+"

        val firstNameText: EditText = findViewById(R.id.editNamePat)
        val lastNameText: EditText = findViewById(R.id.editLastNamePat)
        val passwordText: EditText = findViewById(R.id.editPasswordPat)
        val passwordRepeatText: EditText = findViewById(R.id.editPasswordRepeatPat)

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

    private fun updateUser() {
        val firstNameText: EditText = findViewById(R.id.editNamePat)
        val lastNameText: EditText = findViewById(R.id.editLastNamePat)
        val passwordText: EditText = findViewById(R.id.editPasswordPat)

        val firstName = firstNameText.text.toString().trim()
        val lastName = lastNameText.text.toString().trim()
        val password = passwordText.text.toString().trim()

        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserUid = currentUser?.uid

        if (password.isNotEmpty() && currentUser != null) {
            currentUser.updatePassword(password).addOnSuccessListener {
            }.addOnFailureListener { e ->
                showErrorSnackBar("Error updating password: ${e.message}", true)
            }
        }

        val userUpdates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName
        )

        if (password.isNotEmpty()) userUpdates["password"] = password

        val userRef = FirebaseFirestore.getInstance().collection("patients").document(currentUserUid!!)
        userRef.update(userUpdates)
            .addOnSuccessListener {
                showErrorSnackBar("Data updated successfully", false)
            }
            .addOnFailureListener { e ->
                showErrorSnackBar("Error updating data: ${e.message}", true)
            }
    }

    private fun uploadImageToFirestore(imageUri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference
        val profileImagesRef = storageReference.child("profile_images/${System.currentTimeMillis()}.jpg")

        profileImagesRef.putFile(imageUri)
            .addOnSuccessListener {
                profileImagesRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
                    val userRef = FirebaseFirestore.getInstance().collection("patients").document(currentUserUid!!)
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
