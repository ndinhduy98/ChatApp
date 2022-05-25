package com.freezer.chatapp.data.viewmodel

import androidx.lifecycle.ViewModel
import com.freezer.chatapp.data.model.Profile
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MyProfileViewModel @Inject constructor(@Named("user") user: FirebaseUser?,
                                             @Named("database") database: FirebaseFirestore
) : ViewModel() {
    var myProfile: Profile = Profile()

    init {
        if (user != null) {
            database.collection("profiles").document(user.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        myProfile = task.result.toObject(Profile::class.java)!!
                    }
                }
        }
    }
}