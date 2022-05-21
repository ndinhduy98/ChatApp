package com.freezer.chatapp.ui.main

import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.freezer.chatapp.R
import com.freezer.chatapp.data.model.*
import com.freezer.chatapp.data.viewmodel.ChatGroupsViewModel
import com.freezer.chatapp.data.viewmodel.ContactsViewModel
import com.freezer.chatapp.data.viewmodel.MyProfileViewModel
import com.freezer.chatapp.data.viewmodel.PendingContactsViewModel
import com.freezer.chatapp.databinding.ActivityMainBinding
import com.freezer.chatapp.utils.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavView.setupWithNavController(navController)

        // Retrieve profile
        val myProfileViewModel = ViewModelProvider(this)[MyProfileViewModel::class.java]

        val user = FirebaseAuth.getInstance().currentUser

        val database = Firebase.firestore

        if (user != null) {
            database.collection("profiles").document(user.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        myProfileViewModel.myProfile = task.result.toObject(Profile::class.java)!!
                    }
                }
        }

        // Retrieve pending contacts
        val pendingContactsViewModel = ViewModelProvider(this)[PendingContactsViewModel::class.java]
        val contactsViewModel = ViewModelProvider(this)[ContactsViewModel::class.java]
        val chatGroupsViewModel = ViewModelProvider(this)[ChatGroupsViewModel::class.java]

        if (user != null) {
            database.collection("pending_requests")
                .document(user.uid)
                .collection("requests")
                .addSnapshotListener { snapshot, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        snapshot?.documentChanges?.forEach { documentChange ->
                            when (documentChange.type) {
                                DocumentChange.Type.ADDED -> {
                                    val pendingContactRequest =
                                        documentChange.document.toObject(PendingContactRequest::class.java)
                                    if (pendingContactRequest.status == PendingContactRequest.Status.PENDING) {
                                        val pendingContactProfile =
                                            FirestoreUtils().getProfile(pendingContactRequest.from)
                                        val pendingContact = PendingContact(
                                            pendingContactProfile,
                                            pendingContactRequest
                                        )
                                        pendingContactsViewModel.add(pendingContact)
                                    }
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    val pendingContactRequest =
                                        documentChange.document.toObject(PendingContactRequest::class.java)
                                    if (pendingContactRequest.status != PendingContactRequest.Status.PENDING) {
                                        pendingContactsViewModel.remove(pendingContactRequest)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }

            // Retrieve contacts

            database.collection("contacts")
                .document(user.uid)
                .collection("contacts_list")
                .addSnapshotListener { snapshot, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        snapshot?.documentChanges?.forEach { documentChange ->
                            if (documentChange.type == DocumentChange.Type.ADDED) {
                                val profileRef = documentChange.document.get("ref")
                                if (profileRef is DocumentReference) {
                                    val profile =
                                        profileRef.get().await().toObject(Profile::class.java)
                                    profile?.let { contactsViewModel.add(profile) }
                                }
                            }
                        }
                    }
                }

            // Retrieve chat group
            database.collection("groups")
                .whereArrayContains("members", user.uid)
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.documentChanges?.forEach { documentChange ->
                        if(documentChange.type == DocumentChange.Type.ADDED) {
                            val chatGroup = documentChange.document.toObject(ChatGroup::class.java)
                            database.collection("message").document(chatGroup.id)
                                .collection("messages")
                                .orderBy("createdAt", Query.Direction.DESCENDING)
                                .limit(1)
                                .addSnapshotListener { snapshot, _ ->
                                    snapshot?.documentChanges?.forEach { messageDocumentChange ->
                                        if(messageDocumentChange.type == DocumentChange.Type.ADDED) {
                                            if(messageDocumentChange.document.get("type") == MessageType.TEXT) {
                                                chatGroup.recentMessage?.postValue(messageDocumentChange.document.toObject(TextMessage::class.java))
                                            } else {
                                                chatGroup.recentMessage?.postValue(messageDocumentChange.document.toObject(ImageMessage::class.java))
                                            }
                                        }
                                    }
                                }
                            chatGroupsViewModel.add(chatGroup)
                        }
                    }
                }
        }
    }

    fun setBottomNavigationVisibility(visibility: Int) {
        binding.bottomNavView.visibility = visibility
    }
}