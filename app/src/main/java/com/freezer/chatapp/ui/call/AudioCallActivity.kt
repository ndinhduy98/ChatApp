package com.freezer.chatapp.ui.call

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.freezer.chatapp.R
import com.freezer.chatapp.data.model.Call
import com.freezer.chatapp.data.viewmodel.AudioCallViewModel
import com.freezer.chatapp.databinding.ActivityCallAudioBinding
import dagger.hilt.android.AndroidEntryPoint
import permissions.dispatcher.*
import java.util.*
import javax.inject.Inject

@RuntimePermissions
@AndroidEntryPoint
class AudioCallActivity : AppCompatActivity(){
    private lateinit var binding : ActivityCallAudioBinding

    @Inject
    lateinit var audioCallViewModelAssistedFactory: AudioCallViewModel.AssistedFactory
    private val audioCallViewModel: AudioCallViewModel by viewModels {
        AudioCallViewModel.providesFactory(audioCallViewModelAssistedFactory, intent.extras!!)
    }

    // Timeout timer
    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCallAudioBinding.inflate(layoutInflater)
        binding.audioCallViewModel = audioCallViewModel
        setContentView(binding.root)

        binding.imageButtonCallEnd.setOnClickListener {
            Toast.makeText(this, "Call ended", Toast.LENGTH_SHORT).show()
            audioCallViewModel.endCall()
            finish()
        }

        binding.tgCallMic.setOnCheckedChangeListener { _, isChecked ->
            audioCallViewModel.onMicClick(isChecked)
        }

        binding.tgCallSpeaker.setOnCheckedChangeListener { _, isChecked ->
            audioCallViewModel.onSpeakerClick(isChecked)
        }

        if(intent.extras?.getString("notification_action") == "hang_up") {
            finish()
        }

        audioCallViewModel.callStatus.observe(this) { callStatus ->
            when(callStatus) {
                Call.Status.CALL_STATUS_RINGING -> {

                }
                Call.Status.CALL_STATUS_CONNECTED -> {
                    binding.textViewCallStatus.text = "Connected"
                    binding.chronometerCall.start()
                }
                Call.Status.CALL_STATUS_ENDED -> {
                    audioCallViewModel.endCall()
                    finish()
                }
            }
        }
        initializeWithPermissionCheck()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    fun initialize() {
        audioCallViewModel.initialize()
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

    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
    }
}