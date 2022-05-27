package com.freezer.chatapp.data.viewmodel

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.freezer.chatapp.data.model.Call
import com.freezer.chatapp.data.model.DeliveryStatus
import com.freezer.chatapp.data.model.Profile
import com.freezer.chatapp.data.model.TextMessage
import com.freezer.chatapp.fcm.FirebaseMessagingService
import com.freezer.chatapp.webrtc.AppSdpObserver
import com.freezer.chatapp.webrtc.AudioRTCClient
import com.freezer.chatapp.webrtc.CallMode
import com.freezer.chatapp.webrtc.PeerConnectionObserver
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ActivityContext
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import javax.inject.Named

class AudioCallViewModel @AssistedInject constructor(
    @Named("user") private val user: FirebaseUser?,
    @Named("database") private val database: FirebaseFirestore,
    @ActivityContext private val context: Context,
    @Assisted private val extras: Bundle
) : ViewModel() {
    // Firestore ref
    private lateinit var callRef: DocumentReference
    private lateinit var callInfoRef: DocumentReference

    // WebRTC
    private lateinit var audioRtcClient: AudioRTCClient

    // Audio manager
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Call status
    val callStatus = MutableLiveData<String>()

    // SDP Observer
    private val sdpOfferObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
            // Save offer to Firestore
            if (p0 != null) {
                val data = mapOf("offer" to p0, "answer" to null)

                callRef.set(data)
            }
        }
    }

    private val sdpAnswerObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(sessionDescription: SessionDescription?) {
            super.onCreateSuccess(sessionDescription)
            // Save answer to Firestore
            if (sessionDescription != null) {
                callRef.update("answer", sessionDescription)
            }
        }
    }

    fun initialize() {
        // Set speaker to front speaker
        audioManager.mode = AudioManager.MODE_IN_CALL
        audioManager.isSpeakerphoneOn = false

        callInfoRef = database.collection("calls_info").document()
        callRef = database.collection("calls").document(callInfoRef.id)

        when(extras.getString("calling_mode")) {
            CallMode.CALL_MODE_OFFER -> {
                // Retrieve target profile
                val profile = extras.getParcelable<Profile>("profile")
                val groupId = extras.getString("group_id")

                callInfoRef.set(
                    Call(
                        uid = callRef.id,
                        from = user!!.uid,
                        to = profile!!.uid,
                        callType = Call.Type.CALL_TYPE_AUDIO
                    )
                ).addOnSuccessListener {
                    initializeCall()
                }

                var listener: ListenerRegistration? = null
                listener = callInfoRef.addSnapshotListener { snapshot, _ ->
                    val data = snapshot?.toObject(Call::class.java)
                    if (data != null) {
                        if (data.status != "RINGING") {
                            // Remove notification
                            val manager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
                            manager.cancel(FirebaseMessagingService.NOTIFICATION_ID)
                            listener?.remove()
                            callStatus.postValue(Call.Status.CALL_STATUS_ENDED)
                        }
                    }
                }

                // Save into message
                groupId?.let {
                    database.collection("message")
                        .document(it)
                        .collection("messages")
                        .add(
                            TextMessage(
                            text = "Audio call",
                            sendBy = user.uid,
                            deliveryStatus = DeliveryStatus.SENT
                        )
                        )
                }
            }
            CallMode.CALL_MODE_ANSWER -> {
                // Retrieve
                val callData = extras.getParcelable<Call>("call_data")

                callInfoRef = database.collection("calls_info").document(callData!!.uid)
                callRef = database.collection("calls").document(callData.uid)

                var listener: ListenerRegistration? = null
                listener = callInfoRef.addSnapshotListener { snapshot, _ ->
                    val data = snapshot?.toObject(Call::class.java)
                    if (data != null) {
                        if (data.status != "RINGING") {
                            // Remove notification
                            val manager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
                            manager.cancel(FirebaseMessagingService.NOTIFICATION_ID)
                            listener?.remove()
                            callStatus.postValue(Call.Status.CALL_STATUS_ENDED)
                        }
                    }
                }
                answerCall()
            }
        }
    }

    fun onMicClick(isChecked: Boolean) {
        audioManager.isMicrophoneMute = isChecked
    }

    fun onSpeakerClick(isChecked: Boolean) {
        if(isChecked) {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = true
        } else {
            audioManager.mode = AudioManager.MODE_IN_CALL
            audioManager.isSpeakerphoneOn = false
        }
    }

    private fun initializeCall() {
        audioRtcClient = AudioRTCClient(context,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    audioRtcClient.addIceCandidate(p0)

                    // Store Ice Candidate into Firestore
                    if (p0 != null) {
                        callRef.collection("offerCandidates").add(p0)
                    }
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    super.onConnectionChange(newState)

                    when(newState) {
                        PeerConnection.PeerConnectionState.CONNECTING -> {

                        }
                        PeerConnection.PeerConnectionState.CONNECTED -> {
                            callStatus.postValue(Call.Status.CALL_STATUS_CONNECTED)
                        }
                        PeerConnection.PeerConnectionState.CLOSED -> {
                            callStatus.postValue(Call.Status.CALL_STATUS_ENDED)
                            endCall()
                        }

                        else -> {}
                    }
                }
            })

        audioRtcClient.startAudioCapture()
        audioRtcClient.call(sdpOfferObserver)

        // Listen to remote SDP
        callRef.addSnapshotListener { snapshot, _ ->
            val data = snapshot?.data?.get("answer")
            if(data != null) {
                val dataHashMap = data as HashMap<String, String>
                val description = dataHashMap["description"]
                val answerDescription = SessionDescription(SessionDescription.Type.ANSWER, description)
                audioRtcClient.setRemoteDescription(answerDescription)
            }
        }

        // Listen to remote ICE candidates
        callRef.collection("answerCandidates").addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { change ->
                if(change.type == DocumentChange.Type.ADDED) {
                    val candidate = change.document.data
                    val iceCandidate = IceCandidate(
                        candidate["sdpMid"] as String?,
                        (candidate["sdpMLineIndex"] as Long).toInt(),
                        candidate["sdp"] as String?
                    )
                    audioRtcClient.addIceCandidate(iceCandidate)
                }
            }
        }
    }

    private fun answerCall() {
        // Initialize WebRTC
        audioRtcClient = AudioRTCClient(context,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    audioRtcClient.addIceCandidate(p0)

                    // Store Ice Candidate into Firestore
                    if (p0 != null) {
                        callRef.collection("answerCandidates").add(p0)
                    }
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    super.onConnectionChange(newState)

                    when(newState) {
                        PeerConnection.PeerConnectionState.CONNECTING -> {

                        }
                        PeerConnection.PeerConnectionState.CONNECTED -> {
                            callStatus.postValue(Call.Status.CALL_STATUS_CONNECTED)
                        }
                        PeerConnection.PeerConnectionState.CLOSED -> {
                            callStatus.postValue(Call.Status.CALL_STATUS_ENDED)
                            endCall()
                        }

                        else -> {}
                    }
                }
            })

        callRef.get().addOnSuccessListener { doc ->
            val desc = doc.get("offer")
            if (desc != null) {
                val descHashMap = desc as HashMap<String, String>
                val description = descHashMap["description"]
                val offerDescription = SessionDescription(SessionDescription.Type.OFFER, description)
                audioRtcClient.setRemoteDescription(offerDescription)

                audioRtcClient.startAudioCapture()
                audioRtcClient.answer(sdpAnswerObserver)

                // Listen to remote ICE candidates
                callRef.collection("offerCandidates").addSnapshotListener { snapshot, _ ->
                    snapshot?.documentChanges?.forEach { change ->
                        if(change.type == DocumentChange.Type.ADDED) {
                            val candidate = change.document.data
                            val iceCandidate = IceCandidate(
                                candidate["sdpMid"] as String?,
                                (candidate["sdpMLineIndex"] as Long).toInt(),
                                candidate["sdp"] as String?
                            )
                            audioRtcClient.addIceCandidate(iceCandidate)
                        }
                    }
                }
            }
        }
    }

    fun endCall() {
        audioRtcClient.end()
        callInfoRef.update("status", Call.Status.CALL_STATUS_ENDED)
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory{
        fun create(@Named("extras") extras: Bundle): AudioCallViewModel
    }

    companion object {
        fun providesFactory(assistedFactory: AssistedFactory, extras: Bundle): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(extras) as T
            }
        }
    }
}