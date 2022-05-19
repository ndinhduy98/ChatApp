package com.freezer.chatapp.data.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.Date
import kotlin.collections.ArrayList

object ChatGroupType {
    const val GROUP_CHAT = "GROUP"
    const val PRIVATE_CHAT = "PRIVATE"
}

@Parcelize
data class ChatGroup(
    val id: String = "",
    @ServerTimestamp
    var createdAt: Date? = null,
    var createdBy: String = "",
    var members: ArrayList<String>? = null,
    var name: String? = null,
    var recentMessage: @RawValue Message? = null,
    var type: String = "null",
    @Exclude
    var profile : Profile? = null
) : Parcelable