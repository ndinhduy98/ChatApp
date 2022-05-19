package com.freezer.chatapp.data.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Call (
    var uid: String = "",
    var from: String = "",
    var to: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    var duration: Long = 0L,
    var status: String = Status.CALL_STATUS_RINGING,
    var callType: String = ""
) : Parcelable {
    object Status {
        const val CALL_STATUS_RINGING = "RINGING"
        const val CALL_STATUS_ENDED = "ENDED"
    }
    object Type {
        const val CALL_TYPE_AUDIO = "AUDIO"
        const val CALL_TYPE_VIDEO = "VIDEO"
    }
}