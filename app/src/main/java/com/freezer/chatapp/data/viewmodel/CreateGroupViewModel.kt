package com.freezer.chatapp.data.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class CreateGroupViewModel @Inject constructor(@Named("user") user: FirebaseUser?,
                                              @Named("database") database: FirebaseFirestore
) :ViewModel() {
    val chatGroupMembers = arrayListOf<String>()
    val chatGroupName = MutableLiveData<String>()

    fun addMember(uid: String) {
        chatGroupMembers.add(uid)
    }

    fun removeMember(uid: String) {
        chatGroupMembers.remove(uid)
    }
}