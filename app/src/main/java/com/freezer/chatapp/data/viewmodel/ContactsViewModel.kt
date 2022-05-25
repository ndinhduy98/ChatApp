package com.freezer.chatapp.data.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel
import com.freezer.chatapp.BR
import com.freezer.chatapp.data.model.Profile
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ContactsViewModel @Inject constructor(@Named("user") user: FirebaseUser?,
                                            @Named("database") database: FirebaseFirestore)
    : Observable, ViewModel() {
    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }

    @get:Bindable
    var contactProfiles = ArrayList<Profile>()
        set(value) {
            field = value
            callbacks.notifyChange(this, BR.contactProfiles)
        }

    fun remove(profile: Profile) {
        contactProfiles.remove(profile)
        callbacks.notifyChange(this, BR.contactProfiles)
    }

    fun add(profile: Profile) {
        contactProfiles.add(profile)
        callbacks.notifyChange(this, BR.contactProfiles)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }

    init {
        if(user != null) {
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
                                    profile?.let { add(profile) }
                                }
                            }
                        }
                    }
                }
        }
    }
}