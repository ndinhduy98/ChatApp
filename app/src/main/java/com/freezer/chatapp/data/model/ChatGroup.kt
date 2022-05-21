package com.freezer.chatapp.data.model

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
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
    @Exclude
    var recentMessage: @RawValue MutableLiveData<Message?>? = MutableLiveData(),
    var type: String = "",
    var profileRef : @RawValue DocumentReference? = null
) : Parcelable, ViewModel()