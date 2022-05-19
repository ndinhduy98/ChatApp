package com.freezer.chatapp.utils

import android.util.Log
import com.freezer.chatapp.data.model.RegistrationToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FCMUtils {
    fun sendRegistrationToken(token: String) {
        val user = FirebaseAuth.getInstance().currentUser

        val db = Firebase.firestore
        val registrationToken =
            user?.let {
                RegistrationToken(
                    uid = it.uid,
                    registrationToken = token,
                    modifiedAt = FieldValue.serverTimestamp()
                )
            }

        if (user != null && registrationToken != null) {
            db.collection("fcm_token").document(user.uid).set(registrationToken)
                .addOnCompleteListener { task ->
                    Log.d("SEND_FCM_TOKEN", task.isSuccessful.toString())
                }
        }
    }
}