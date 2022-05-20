package com.freezer.chatapp.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class PendingContactRequest(
    var from: String = "",
    var to: String = "",
    var status: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
) {
    object Status {
        const val PENDING = "PENDING"
        const val APPROVED = "APPROVED"
        const val REJECTED = "REJECTED"
    }
}