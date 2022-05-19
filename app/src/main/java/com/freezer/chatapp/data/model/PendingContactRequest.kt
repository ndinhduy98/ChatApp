package com.freezer.chatapp.data.model

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

object PendingContactRequestStatus {
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
}

data class PendingContactRequest(
    val from: String,
    val to: String,
    var status: String,
//    @ServerTimestamp
//    val createdAt: Date? = null
)