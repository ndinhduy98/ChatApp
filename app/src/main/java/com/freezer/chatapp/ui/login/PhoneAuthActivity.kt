package com.freezer.chatapp.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import com.freezer.chatapp.databinding.ActivityPhoneAuthBinding
import com.freezer.chatapp.ui.main.MainActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneAuthBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private var phoneNumber: String = ""

    private val getTokenResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
        // Handle result from SMS code verification
        if (result != null) {
            if(result.resultCode == Activity.RESULT_OK) {
                // Retrieve phone number verification data

                // If new user, go to Your Profile
                startActivity(Intent(this, YourProfileActivity::class.java))
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@PhoneAuthActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationId, token)
                // Start SMS Code verification activity
                val bundle = Bundle()
                bundle.putString("verification_id", verificationId)
                bundle.putParcelable("verification_token", token)
                bundle.putString("phone_number", phoneNumber)
                val intent = Intent(this@PhoneAuthActivity, VerifySmsCodeActivity::class.java)
                intent.putExtras(bundle)
                getTokenResult.launch(intent)
            }
        }

        binding.buttonContinue.setOnClickListener {
            phoneNumber = "+${binding.countryCodeHolder.selectedCountryCodeAsInt}${binding.editTextPhoneNumber.text.toString()}"
            startPhoneNumberVerification(phoneNumber)
        }
    }


    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(90L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}