package com.freezer.chatapp.data.model.webrtc

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.firestore.Exclude

enum class MessageType(val value: String) {
    SDPMessage("sdp"),
    ICEMessage("ice"),
    PeerLeft("peer-left");

    override fun toString() = value
}

open class ClientMessage(@get:Exclude val type: MessageType)

data class SDPMessage(var sdp: String = "") : ClientMessage(MessageType.SDPMessage)
data class ICEMessage(var label: Int = 0, var id: String = "", var candidate: String = "") : ClientMessage(MessageType.ICEMessage)
class PeerLeft : ClientMessage(MessageType.PeerLeft)

class FirebaseSignaler(val callerID: String) {

    private val TAG = "FirebaseSignaler"

    private val database = FirebaseDatabase.getInstance()
    private val refToData = database.getReference(FirebaseData.getCallDataPath(callerID))
    private val refToStatus = database.getReference(FirebaseData.getCallStatusPath(callerID))
    private val refMyData = database.getReference(FirebaseData.getCallDataPath(FirebaseData.myID))
    private val refMyStatus = database.getReference(FirebaseData.getCallStatusPath(FirebaseData.myID))
    var messageHandler: ((ClientMessage) -> Unit)? = null

    private val dataListener: ChildEventListener = object : ChildEventListener {
        fun onMessage(dataSnapshot: DataSnapshot) {
            if (!dataSnapshot.exists()) return
            val type = dataSnapshot.key
            val clientMessage =
                when (type) {
                    "sdp" ->
                        dataSnapshot.getValue(SDPMessage::class.java)
                    "ice" ->
                        dataSnapshot.getValue(ICEMessage::class.java)
                    else ->
                        null
                }
            Log.i(TAG, "FirebaseSignaler: Decoded message as ${clientMessage?.type}")
            if (clientMessage != null) messageHandler!!.invoke(clientMessage)
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
            onMessage(dataSnapshot)
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
            onMessage(dataSnapshot)
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }


        override fun onCancelled(e: DatabaseError) {
            Log.e(TAG, "databaseError:", e.toException())
        }
    }

    private val statusListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists() && !dataSnapshot.getValue(Boolean::class.java)!!)
                messageHandler?.invoke(PeerLeft())
        }

        override fun onCancelled(e: DatabaseError) {
            Log.e(TAG, "databaseError:", e.toException())
        }
    }


    fun init() {
        listen()
    }

    private fun listen() {
        refMyData.onDisconnect().removeValue()
        refMyStatus.onDisconnect().setValue(false)
        refMyStatus.setValue(true)

        refToData.addChildEventListener(dataListener)
        refToStatus.addValueEventListener(statusListener)
    }

    private fun send(clientMessage: ClientMessage) {
        val reference = refMyData.child(clientMessage.type.value)
        reference.setValue(clientMessage).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "FirebaseSignaler: Sended succesfully ${clientMessage.type.value}")
//                reference.removeValue()
            } else
                Log.w(TAG, "Message of type '${clientMessage.type.value}' can't be sent to the server")
        }
    }

    fun close() {
        Log.w(TAG, "close")
        refToData.removeEventListener(dataListener)
        refToStatus.removeEventListener(statusListener)
        refMyData.removeValue()
        refMyStatus.setValue(false)
//        webSocket?.close(1000, null)
    }

    fun sendSDP(sdp: String) {
        Log.w(TAG, "sendSDP : " + sdp)
        send(SDPMessage(sdp))
    }

    fun sendCandidate(label: Int, id: String, candidate: String) {
        Log.w(TAG, "sendCandidate :  $label $id $candidate")
        send(ICEMessage(label, id, candidate))
    }
}