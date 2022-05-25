package com.freezer.chatapp.data.viewmodel

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel
import com.freezer.chatapp.BR
import com.freezer.chatapp.data.model.PendingContact
import com.freezer.chatapp.data.model.PendingContactRequest
import com.freezer.chatapp.utils.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

@RequiresApi(Build.VERSION_CODES.N)
@HiltViewModel
class PendingContactsViewModel @Inject constructor(@Named("user") private val user: FirebaseUser?,
                                                   @Named("database") private val database: FirebaseFirestore)
    : Observable, ViewModel() {
    @get:Bindable
    var pendingIndicator = View.INVISIBLE

    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }
    @get:Bindable
    var pendingContacts = ArrayList<PendingContact>()
        set(value) {
            field = value
            pendingIndicator = if (pendingContacts.isNotEmpty()) View.VISIBLE else View.GONE
            callbacks.notifyChange(this, BR.pendingContacts)
        }

    fun remove(pendingContact: PendingContact) {
        pendingContacts.remove(pendingContact)
        pendingIndicator = if (pendingContacts.isNotEmpty()) View.VISIBLE else View.GONE
        callbacks.notifyChange(this, BR.pendingContacts)
        callbacks.notifyChange(this, BR.pendingIndicator)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun remove(pendingContactRequest: PendingContactRequest) {
        pendingContacts.removeIf { pendingContact ->
            pendingContact.request.from == pendingContactRequest.from
                    && pendingContact.request.to == pendingContactRequest.to
        }
        pendingIndicator = if (pendingContacts.isNotEmpty()) View.VISIBLE else View.GONE
        callbacks.notifyChange(this, BR.pendingContacts)
        callbacks.notifyChange(this, BR.pendingIndicator)
    }

    fun add(pendingContact: PendingContact) {
        pendingContacts.add(pendingContact)
        pendingIndicator = if (pendingContacts.isNotEmpty()) View.VISIBLE else View.GONE
        callbacks.notifyChange(this, BR.pendingContacts)
        callbacks.notifyChange(this, BR.pendingIndicator)
    }


    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }

    init {
        if(user != null) {
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
                                        add(pendingContact)
                                    }
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    val pendingContactRequest =
                                        documentChange.document.toObject(PendingContactRequest::class.java)
                                    if (pendingContactRequest.status != PendingContactRequest.Status.PENDING) {
                                        remove(pendingContactRequest)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
        }
    }

    fun setPendingContactRequest(pendingContact: PendingContact, status: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (user != null) {
                val queryResults = database.collection("pending_requests")
                    .document(user.uid)
                    .collection("requests")
                    .whereEqualTo("from", pendingContact.request.from)
                    .whereEqualTo("to", pendingContact.request.to)
                    .get().await()

                queryResults.forEach { pendingContactRequest ->
                    database.collection("pending_requests")
                        .document(user.uid)
                        .collection("requests")
                        .document(pendingContactRequest.id)
                        .update("status", status)
                }
            }
        }
    }
}