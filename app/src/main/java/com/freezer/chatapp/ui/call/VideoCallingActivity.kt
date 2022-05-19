package com.freezer.chatapp.ui.call

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.freezer.chatapp.data.model.Call
import com.freezer.chatapp.data.model.Profile
import com.freezer.chatapp.webrtc.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import permissions.dispatcher.*
import com.freezer.chatapp.R
import com.freezer.chatapp.databinding.ActivityCallingVideoBinding
import com.google.firebase.firestore.DocumentChange
import java.util.*
import kotlin.collections.HashMap


@RuntimePermissions
class VideoCallingActivity : AppCompatActivity(){
    private lateinit var binding : ActivityCallingVideoBinding

    private lateinit var database: FirebaseFirestore
    private lateinit var user: FirebaseUser

    // Timeout timer
    private lateinit var timer: Timer

    // Firestore Ref
    private lateinit var callRef: DocumentReference
    private lateinit var callInfoRef: DocumentReference

    // WebRTC
    private lateinit var videoRtcClient: VideoRTCClient

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

        binding = ActivityCallingVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        database = Firebase.firestore
        user = FirebaseAuth.getInstance().currentUser!!

        when(intent.extras?.getString("calling_mode")) {
            CallMode.CALL_MODE_OFFER -> {
                // Retrieve target profile
                val profile = intent.extras?.getParcelable<Profile>("profile")

                // Send call data to database
                callInfoRef = database.collection("calls_info").document()
                callRef = database.collection("calls").document(callInfoRef.id)

                callInfoRef.set(Call(
                    uid = callRef.id,
                    from = user.uid,
                    to = profile!!.uid,
                    callType = Call.Type.CALL_TYPE_VIDEO
                )).addOnSuccessListener {
                    initializeCallWithPermissionCheck()
                }
            }
            CallMode.CALL_MODE_ANSWER -> {
                // Retrieve
                val callData = intent.extras?.getParcelable<Call>("call_data")

                callInfoRef = database.collection("calls_info").document(callData!!.uid)
                callRef = database.collection("calls").document(callData.uid)

                answeringCallWithPermissionCheck()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        videoRtcClient.end()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    fun initializeCall() {
        // Initialize WebRTC
        videoRtcClient = VideoRTCClient(this,
        object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                videoRtcClient.addIceCandidate(p0)

                // Store Ice Candidate into Firestore
                if (p0 != null) {
                    callRef.collection("offerCandidates").add(p0)
                }
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                p0?.videoTracks?.get(0)?.addSink(binding.surfaceViewRemoteRTC)
            }
        })

        videoRtcClient.initSurfaceView(binding.surfaceViewRemoteRTC)
        videoRtcClient.initSurfaceView(binding.surfaceViewLocalRTC)
        videoRtcClient.startLocalAudioVideoCapture(binding.surfaceViewLocalRTC)

        videoRtcClient.call(sdpOfferObserver)

        // Listen to remote SDP
        callRef.addSnapshotListener { snapshot, _ ->
            val data = snapshot?.data?.get("answer")
            if(data != null) {
                val dataHashMap = data as HashMap<String, String>
                val description = dataHashMap["description"]
                val answerDescription = SessionDescription(SessionDescription.Type.ANSWER, description)
                videoRtcClient.setRemoteDescription(answerDescription)
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
                    videoRtcClient.addIceCandidate(iceCandidate)
                }
            }
        }

        // End call if exceed 30 seconds

    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    fun answeringCall() {
        // Initialize WebRTC
        videoRtcClient = VideoRTCClient(this,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    videoRtcClient.addIceCandidate(p0)

                    // Store Ice Candidate into Firestore
                    if (p0 != null) {
                        callRef.collection("answerCandidates").add(p0)
                    }
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    p0?.videoTracks?.get(0)?.addSink(binding.surfaceViewRemoteRTC)
                }
            })

        videoRtcClient.initSurfaceView(binding.surfaceViewRemoteRTC)
        videoRtcClient.initSurfaceView(binding.surfaceViewLocalRTC)
        videoRtcClient.startLocalAudioVideoCapture(binding.surfaceViewLocalRTC)

        callRef.get().addOnSuccessListener { doc ->
            val desc = doc.get("offer")
            if (desc != null) {
                val descHashMap = desc as HashMap<String, String>
                val description = descHashMap["description"]
                val offerDescription = SessionDescription(SessionDescription.Type.OFFER, description)
                videoRtcClient.setRemoteDescription(offerDescription)
                videoRtcClient.answer(sdpAnswerObserver)

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
                            videoRtcClient.addIceCandidate(iceCandidate)
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
        Toast.makeText(this, "Please grant microphone permission", Toast.LENGTH_SHORT)
        finish()
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    fun showRationaleForCamera(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_camera_rationale, request)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraDenied() {

    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onCameraNeverAskAgain() {
        Toast.makeText(this, "Please grant camera permission", Toast.LENGTH_SHORT)
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