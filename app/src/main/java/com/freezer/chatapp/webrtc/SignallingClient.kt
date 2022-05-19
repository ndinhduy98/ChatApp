package com.freezer.chatapp.webrtc

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SignallingClient(private val callRefId: String, private val userUid: String) : CoroutineScope{
    private val job = Job()
    override val coroutineContext = Dispatchers.IO + job

    init {
        connect()
    }

    private fun connect() = launch {
        val database = Firebase.firestore

        // Initialize signalling
        val callDoc = database.collection("call").document(callRefId)
        val offerCandidates = callDoc.collection("offerCandidates")
        val answerCandidates = callDoc.collection("answerCandidates")

    }
}