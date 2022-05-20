package com.freezer.chatapp.ui.call

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.freezer.chatapp.R
import com.freezer.chatapp.data.model.Call
import com.freezer.chatapp.data.model.DeliveryStatus
import com.freezer.chatapp.data.model.Profile
import com.freezer.chatapp.data.model.TextMessage
import com.freezer.chatapp.databinding.ActivityCallingAudioBinding
import com.freezer.chatapp.fcm.FirebaseMessagingService
import com.freezer.chatapp.webrtc.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import permissions.dispatcher.*
import java.util.*
import kotlin.collections.HashMap

@RuntimePermissions
class AudioCallingActivity : AppCompatActivity(){
    private lateinit var binding : ActivityCallingAudioBinding

    private lateinit var database: FirebaseFirestore
    private lateinit var user: FirebaseUser

    // Timeout timer
    private lateinit var timer: Timer

    // Firestore Ref
    private lateinit var callRef: DocumentReference
    private lateinit var callInfoRef: DocumentReference

    // WebRTC
    private lateinit var audioRtcClient: AudioRTCClient

    // Audio manager
    private lateinit var audioManager: AudioManager

    // Speaker switch

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCallingAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set speaker to front speaker
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_CALL
        audioManager.isSpeakerphoneOn = false

        binding.imageButtonCallEnd.setOnClickListener {
            Toast.makeText(this, "Call ended", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.tgCallMic.setOnCheckedChangeListener { _, isChecked ->
            audioManager.isMicrophoneMute = isChecked
        }

        binding.tgCallSpeaker.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                audioManager.mode = AudioManager.MODE_NORMAL
                audioManager.isSpeakerphoneOn = true
            } else {
                audioManager.mode = AudioManager.MODE_IN_CALL
                audioManager.isSpeakerphoneOn = false
            }
        }

        database = Firebase.firestore
        user = FirebaseAuth.getInstance().currentUser!!

        if(intent.extras?.getString("notification_action") == "hang_up") {
            finish()
        }

        when(intent.extras?.getString("calling_mode")) {
            CallMode.CALL_MODE_OFFER -> {
                // Retrieve target profile
                val profile = intent.extras?.getParcelable<Profile>("profile")
                val groupId = intent.extras?.getString("group_id")

                // Send call data to database
                callInfoRef = database.collection("calls_info").document()
                callRef = database.collection("calls").document(callInfoRef.id)

                callInfoRef.set(
                    Call(
                        uid = callRef.id,
                        from = user.uid,
                        to = profile!!.uid,
                        callType = Call.Type.CALL_TYPE_AUDIO
                    )
                ).addOnSuccessListener {
                    initializeCallWithPermissionCheck()
                }

                var listener: ListenerRegistration? = null
                listener = callInfoRef.addSnapshotListener { snapshot, _ ->
                    val data = snapshot?.toObject(Call::class.java)
                    if (data != null) {
                        if (data.status != "RINGING") {
                            // Remove notification
                            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                            manager.cancel(FirebaseMessagingService.NOTIFICATION_ID)
                            listener?.remove()
                            finish()
                        }
                    }
                }

                // Save into message
                groupId?.let {
                    database.collection("message")
                        .document(it)
                        .collection("messages")
                        .add(TextMessage(
                            text = "Audio call",
                            sendBy = user.uid,
                            deliveryStatus = DeliveryStatus.SENT
                        ))
                }
            }
            CallMode.CALL_MODE_ANSWER -> {
                // Retrieve
                val callData = intent.extras?.getParcelable<Call>("call_data")

                callInfoRef = database.collection("calls_info").document(callData!!.uid)
                callRef = database.collection("calls").document(callData.uid)

                var listener: ListenerRegistration? = null
                listener = callInfoRef.addSnapshotListener { snapshot, _ ->
                    val data = snapshot?.toObject(Call::class.java)
                    if (data != null) {
                        if (data.status != "RINGING") {
                            // Remove notification
                            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                            manager.cancel(FirebaseMessagingService.NOTIFICATION_ID)
                            listener?.remove()
                            finish()
                        }
                    }
                }

                answeringCallWithPermissionCheck()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        audioRtcClient.end()
        callInfoRef.update("status", Call.Status.CALL_STATUS_ENDED)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    fun initializeCall() {
        // Initialize WebRTC
        audioRtcClient = AudioRTCClient(this,
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
                            binding.textViewCallStatus.text = "Connected"
                            binding.chronometerCall.start()
                        }
                        PeerConnection.PeerConnectionState.CLOSED -> {
                            finish()
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

        // End call if exceed 30 seconds

    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    fun answeringCall() {
        // Initialize WebRTC
        audioRtcClient = AudioRTCClient(this,
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
                            binding.textViewCallStatus.text = "Connected"
                            binding.chronometerCall.start()
                        }
                        PeerConnection.PeerConnectionState.CLOSED, PeerConnection.PeerConnectionState.DISCONNECTED -> {
                            finish()
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

    @OnShowRationale(Manifest.permission.RECORD_AUDIO)
    fun showRationaleForAudio(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_record_audio_rationale, request)
    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    fun onRecordAudioDenied() {

    }

    @OnNeverAskAgain(Manifest.permission.RECORD_AUDIO)
    fun onRecordAudioNeverAskAgain() {
        Toast.makeText(this, "Please grant microphone permission", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
    }
}