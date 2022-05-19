package com.freezer.chatapp.data.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Profile(
    var uid: String =" ",
    var phoneNumber: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var avatarUrl: String = "",
    @ServerTimestamp
    val modifiedAt: Date? = null
) : Parcelable