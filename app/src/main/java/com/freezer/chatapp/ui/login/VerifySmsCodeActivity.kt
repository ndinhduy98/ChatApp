package com.freezer.chatapp.ui.login

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fraggjkee.smsconfirmationview.SmsConfirmationView
import com.freezer.chatapp.databinding.ActivityVerifySmsCodeBinding
import com.freezer.chatapp.utils.LiveDataTimerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit


class VerifySmsCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifySmsCodeBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var phoneNumber: String
    private lateinit var verificationId: String
    private lateinit var token: PhoneAuthProvider.ForceResendingToken

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var mLiveDataTimerViewModel: LiveDataTimerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerifySmsCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: timer
//        mLiveDataTimerViewModel = ViewModelProvider(this)[mLiveDataTimerViewModel::class.java]

        auth = Firebase.auth

        val bundle = intent.extras
        bundle?.let {
            phoneNumber = bundle.getString("phone_number", "")
            binding.textViewEnterSmsCodeInstruction.text = "${binding.textViewEnterSmsCodeInstruction.text}$phoneNumber"
            verificationId = bundle.getString("verification_id", "")
            token = bundle.getParcelable("verification_token")!!
        }

        binding.smsCode.onChangeListener = SmsConfirmationView.OnChangeListener { code, _ ->
            if(code.length == 6) {
                verifyPhoneNumberWithCode(verificationId, code)
            }
        }
    }

//    private fun subscribe() {
//        val elapsedTimeObserver: Observer<Long> =
//            Observer { aLong ->
//                val newText: String = resources.getString(
//                    R.string.seconds, aLong
//                )
//                (findViewById<View>(R.id.timer_textview) as TextView).text = newText
//            }
//        mLiveDataTimerViewModel.elapsedTime.observe(this, elapsedTimeObserver)
//    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    Log.d("LogIn", "Success")
                    val user = task.result.user
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "${task.exception}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun resendVerificationCode(phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken?) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(90L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
    }
}