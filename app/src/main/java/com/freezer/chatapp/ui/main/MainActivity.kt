package com.freezer.chatapp.ui.main

import android.os.Build
import android.os.Bundle
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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("MainActivity", "${exception.printStackTrace()}")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
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

        if (user != null) {
            database.collection("pending_requests").document(user.uid).addSnapshotListener { snapshot, e ->
                run {
                    val snapshotData = snapshot?.data?.get("requests")
                    if (snapshot != null && snapshotData != null) {
                        if (snapshotData is HashMap<*, *>) {
                            val pendingContactSnapshot = snapshotData as HashMap<String, String>
                            val request = PendingContactRequest(
                                    from = pendingContactSnapshot.getValue("from"),
                                    to = pendingContactSnapshot.getValue("to"),
                                    status = pendingContactSnapshot.getValue("status"))

                            if (request.status != PendingContactRequestStatus.PENDING) {
                                pendingContactsViewModel.pendingContacts.forEach {
                                    if (it.request.from == request.from) {
                                        pendingContactsViewModel.remove(it)
                                    }
                                }
                            } else {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val profile = FirestoreUtils().getProfile(request.from)
                                    val pendingContact = PendingContact(profile, request)
                                    pendingContactsViewModel.add(pendingContact)
                                }
                            }

                        } else {
                            val pendingContacts = FirestoreUtils().parsePendingContacts(snapshotData as ArrayList<HashMap<String, String>>)
                            pendingContactsViewModel.pendingContacts = pendingContacts
                        }
                    }
                }
            }
        }

        // Retrieve contacts
        val contactsViewModel = ViewModelProvider(this)[ContactsViewModel::class.java]

        if (user != null) {
            database.collection("contacts")
                .document(user.uid)
                .addSnapshotListener { snapshot, _ ->
                val snapshotData = snapshot?.data?.get("list")
                if (snapshot != null && snapshotData != null) {
                    if(snapshotData is DocumentReference) {
                        CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
                            val profile = snapshotData.get().await().toObject(Profile::class.java)
                            profile?.let { contactsViewModel.add(profile) }
                        }
                    } else {
                        try {
                            CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
                                for(documentRef in snapshotData as ArrayList<DocumentReference>) {
                                        val profile = documentRef.get().await().toObject(Profile::class.java)
                                        profile?.let { contactsViewModel.add(profile) }
                                }
                            }
                        } catch (e: ClassCastException) {
                            Log.e("MainActivity", "${e.printStackTrace()}")
                        }
                    }
                }
            }
        }

        // Retrieve chat group
        val chatGroupsViewModel = ViewModelProvider(this)[ChatGroupsViewModel::class.java]

        if (user != null) {
            database.collection("groups")
                .whereArrayContains("members", user.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        for (doc in snapshot) {
                            val chatGroup = doc.toObject(ChatGroup::class.java)
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