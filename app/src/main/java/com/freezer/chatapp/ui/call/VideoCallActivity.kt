package com.freezer.chatapp.ui.call

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import permissions.dispatcher.*
import com.freezer.chatapp.R
import com.freezer.chatapp.data.viewmodel.VideoCallViewModel
import com.freezer.chatapp.databinding.ActivityCallVideoBinding
import java.util.*
import javax.inject.Inject

@RuntimePermissions
class VideoCallActivity : AppCompatActivity(){
    private lateinit var binding : ActivityCallVideoBinding

    // Timeout timer
    private lateinit var timer: Timer

    @Inject
    lateinit var videoCallViewModelAssistedFactory: VideoCallViewModel.AssistedFactory
    private val videoCallViewModel: VideoCallViewModel by viewModels {
        VideoCallViewModel.providesFactory(videoCallViewModelAssistedFactory, intent.extras!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCallVideoBinding.inflate(layoutInflater)
        binding.videoCallViewModel = videoCallViewModel

        binding.imageButtonCallEnd.setOnClickListener {
            Toast.makeText(this, "Call ended", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.tgCallMic.setOnCheckedChangeListener { _, isChecked ->
            videoCallViewModel.onMicClick(isChecked)
        }

        setContentView(binding.root)
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
    fun initialize() {
        videoCallViewModel.initialize(binding.surfaceViewLocalRTC, binding.surfaceViewRemoteRTC)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
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

    @OnShowRationale(Manifest.permission.CAMERA)
    fun showRationaleForCamera(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_camera_rationale, request)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraDenied() {

    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onCameraNeverAskAgain() {
        Toast.makeText(this, "Please grant camera permission", Toast.LENGTH_SHORT).show()
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