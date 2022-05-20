package com.freezer.chatapp.ui.login

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.freezer.chatapp.data.model.Profile
import com.freezer.chatapp.data.viewmodel.LoginViewModel
import com.freezer.chatapp.databinding.ActivityYourProfileBinding
import com.freezer.chatapp.ui.main.MainActivity
import com.freezer.chatapp.utils.FCMUtils
import com.freezer.chatapp.utils.getBitmap
import com.freezer.chatapp.utils.reduceBitmapSize
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception

class YourProfileActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityYourProfileBinding
    private lateinit var imageAvatarUri: Uri

    private val imageChooserResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
        if(result != null) {
            if(result.resultCode == Activity.RESULT_OK) {
                imageAvatarUri = result.data!!.data!!
                Glide.with(this)
                    .load(imageAvatarUri)
                    .circleCrop()
                    .into(binding.imageViewAvatar)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityYourProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        retrieveProfile()

        binding.imageButtonAvatar.setOnClickListener {
            showImageChooser()
        }

        binding.buttonProfileSave.setOnClickListener {
            saveProfile()
        }

        // Send FCM token for first time
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if(task.isSuccessful) {
                FCMUtils().sendRegistrationToken(task.result)
            }
        }
    }

    private fun showImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        imageChooserResult.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private fun saveProfile() {
        val user = FirebaseAuth.getInstance().currentUser

        val database = Firebase.firestore

        var avatarRef = FirebaseStorage.getInstance("gs://chatapp-68a8d.appspot.com")
            .reference

        if (user != null && user.phoneNumber != null) {
            var uploadTask: UploadTask.TaskSnapshot? = null

            // Check if Avatar is set
                if(this::imageAvatarUri.isInitialized) {
                    val resizedImageAvatar = imageAvatarUri.getBitmap(contentResolver)?.reduceBitmapSize()
                    val resizedImageAvatarOutputStream = ByteArrayOutputStream()
                    resizedImageAvatar?.compress(Bitmap.CompressFormat.JPEG, 100, resizedImageAvatarOutputStream)
                    val imageAvatarByteArray = resizedImageAvatarOutputStream.toByteArray()
                    // Save to internal memory
                    try {
                        val outputFile = File(filesDir, "${user.uid}_avatar.jpg")
                        outputFile.appendBytes(resizedImageAvatarOutputStream.toByteArray())
                    } catch (e: Exception) {
                        Log.e("SaveAvatar", e.printStackTrace().toString())
                    }
                    val profileScope = CoroutineScope(Dispatchers.IO)
                    profileScope.launch {
                        avatarRef = avatarRef.child("profiles").child(user.uid).child("avatar").child("${user.uid}_avatar.jpg")
                        uploadTask = avatarRef.putBytes(imageAvatarByteArray).await()

                        if(uploadTask?.error != null) {
                            Toast.makeText(this@YourProfileActivity, "Can't upload profile image", Toast.LENGTH_LONG).show()
                        }
                        // Upload complete, proceed to profile
                        val profile = Profile(uid = user.uid,
                            phoneNumber = user.phoneNumber!!,
                            firstName = binding.editTextFirstName.text.toString(),
                            lastName = binding.editTextLastName.text.toString(),
                            avatarUrl = if(uploadTask?.error == null) uploadTask!!.metadata!!.path else "")
                        database.collection("profiles").document(user.uid).set(profile)
                            .addOnSuccessListener {
                                Toast.makeText(this@YourProfileActivity, "Save profile successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@YourProfileActivity, MainActivity::class.java))
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@YourProfileActivity, "Error while saving profile", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }
    }

    private fun retrieveProfile() {
        val user = FirebaseAuth.getInstance().currentUser

        val database = Firebase.firestore

        var avatarRef = FirebaseStorage.getInstance("gs://chatapp-68a8d.appspot.com")
            .reference

        if (user != null) {
            val retrieverScope = CoroutineScope(Dispatchers.IO)
            retrieverScope.launch {
                database.collection("profiles").document(user.uid).get()
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            val document = task.result
                            if(document.exists()) {
                                val profile = document.toObject(Profile::class.java)
                                profile?.let {
                                    runOnUiThread {
                                        binding.editTextFirstName.setText(profile.firstName)
                                        binding.editTextLastName.setText(profile.lastName)
                                    }
                                    if(profile.avatarUrl.isNotEmpty()) {
                                        avatarRef = avatarRef.child(profile.avatarUrl)
                                        val localAvatarFile = File(filesDir, "${user.uid}_avatar.jpg")

                                        val downloadScope = CoroutineScope(Dispatchers.IO)
                                        downloadScope.launch {
                                            val downloadTask = avatarRef.getFile(localAvatarFile).await()
                                            if(downloadTask.error == null) {
                                                runOnUiThread {
                                                    Glide.with(this@YourProfileActivity)
                                                        .load(localAvatarFile)
                                                        .circleCrop()
                                                        .into(binding.imageViewAvatar)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}