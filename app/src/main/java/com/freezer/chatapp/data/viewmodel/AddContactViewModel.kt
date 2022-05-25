package com.freezer.chatapp.data.viewmodel

import android.util.Log
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.freezer.chatapp.BR
import com.freezer.chatapp.data.model.PendingContactRequest
import com.freezer.chatapp.data.model.Profile
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class AddContactViewModel @Inject constructor(@Named("user") private val user: FirebaseUser?,
                                              @Named("database") private val database: FirebaseFirestore
)
    : Observable, ViewModel() {
    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }

    val addContactStatus = MutableLiveData<Boolean>()

    @get:Bindable
    var addContactProfiles: List<Profile> = emptyList()
        set(value) {
            field = value
            callbacks.notifyChange(this, BR.addContactProfiles)
        }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }

    fun findPhoneNumber(phoneNumber: String) {
        val userRef = database.collection("profiles")

        val query = userRef.whereEqualTo("phoneNumber", "+$phoneNumber")
        query.get()
            .addOnSuccessListener { documents ->
                addContactProfiles = (documents.toObjects(Profile::class.java))
            }
            .addOnFailureListener {
                Log.e("Query", it.printStackTrace().toString())
            }
    }

    fun addPendingContact(profile: Profile) {
        user?.let {
            val pendingRequest = PendingContactRequest(
                from = user.uid,
                to = profile.uid,
                status = PendingContactRequest.Status.PENDING
            )
            database.collection("pending_requests")
                .document(profile.uid)
                .collection("requests")
                .add(pendingRequest)
                .addOnSuccessListener {
                    addContactStatus.postValue(true)
                }
                .addOnFailureListener {
                    addContactStatus.postValue(false)
                }
        }
    }
}