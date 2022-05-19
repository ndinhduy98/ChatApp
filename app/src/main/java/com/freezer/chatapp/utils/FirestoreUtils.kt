package com.freezer.chatapp.utils

import com.freezer.chatapp.data.model.PendingContact
import com.freezer.chatapp.data.model.PendingContactRequest
import com.freezer.chatapp.data.model.Profile
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.jvm.Throws

class FirestoreUtils {
    companion object {

    }

    @Throws(ClassCastException::class)
    fun parsePendingContacts(pendingContacts: ArrayList<HashMap<String, String>>) : ArrayList<PendingContact>{
        val pendingContactsList = ArrayList<PendingContact>()
        for(pendingContact in pendingContacts) {
            CoroutineScope(Dispatchers.IO).launch {
                val profile = getProfile(pendingContact.getValue("from"))
                val request = PendingContactRequest(from = pendingContact.getValue("from"),
                                                    to = pendingContact.getValue("to"),
                                                    status = pendingContact.getValue("status"))
                val pendingContactObject = PendingContact(profile, request)
                pendingContactsList.add(pendingContactObject)
            }
        }
        return pendingContactsList
    }

    fun parsePendingContactsWithoutProfiles(pendingContacts: ArrayList<HashMap<String, String>>) : ArrayList<PendingContactRequest> {
        val pendingContactsList = ArrayList<PendingContactRequest>()
        for(pendingContact in pendingContacts) {
            CoroutineScope(Dispatchers.IO).launch {
                val request = PendingContactRequest(from = pendingContact.getValue("from"),
                    to = pendingContact.getValue("to"),
                    status = pendingContact.getValue("status"))
                pendingContactsList.add(request)
            }
        }
        return pendingContactsList
    }

    suspend fun getProfile(uid: String) : Profile? {
        val database = Firebase.firestore
        return database.collection("profiles").document(uid).get().await()
            .toObject(Profile::class.java)
    }
}