package com.freezer.chatapp.data.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel
import com.freezer.chatapp.BR
import com.freezer.chatapp.data.model.ChatGroup
import com.freezer.chatapp.data.model.ImageMessage
import com.freezer.chatapp.data.model.MessageType
import com.freezer.chatapp.data.model.TextMessage
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ChatGroupsViewModel @Inject constructor(@Named("user") user: FirebaseUser?,
                                             @Named("database") database: FirebaseFirestore
)
    : Observable, ViewModel() {
    private val callbacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }

    @get:Bindable
    var chatGroups = ArrayList<ChatGroup>()
        set(value) {
            field = value
            callbacks.notifyChange(this, BR.chatGroups)
        }

    fun remove(chatGroup: ChatGroup) {
        chatGroups.remove(chatGroup)
        callbacks.notifyChange(this, BR.chatGroups)
    }

    fun add(chatGroup: ChatGroup) {
        chatGroups.add(chatGroup)
        callbacks.notifyChange(this, BR.chatGroups)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }

    init {
        if (user != null) {
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
                                                chatGroup.recentMessage?.postValue(messageDocumentChange.document.toObject(
                                                    TextMessage::class.java))
                                            } else {
                                                chatGroup.recentMessage?.postValue(messageDocumentChange.document.toObject(
                                                    ImageMessage::class.java))
                                            }
                                        }
                                    }
                                }
                            add(chatGroup)
                        }
                    }
                }
        }
    }
}