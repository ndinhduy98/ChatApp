package com.freezer.chatapp.data.model

import com.google.firebase.firestore.FieldValue

data class RegistrationToken(
    val uid: String,
    val registrationToken: String,
    val modifiedAt: FieldValue
)