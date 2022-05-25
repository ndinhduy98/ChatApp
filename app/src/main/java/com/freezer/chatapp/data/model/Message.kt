package com.freezer.chatapp.data.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date

object MessageType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
}

object DeliveryStatus {
    const val SENT = "SENT"
    const val RECEIVED = "RECEIVED"
    const val SEEN = "SEEN"
}

interface Message {
    var text: String
    var sendBy: String
    var createdAt: Date?
    var deliveryStatus: String?
    val type: String

    @Exclude
    fun getFormattedCreatedAt(): String? {
        return createdAt?.let { SimpleDateFormat("HH:mm").format(it) }
    }
}

@Parcelize
class TextMessage(override var text: String = "",
                  override var sendBy: String = "",
                  @ServerTimestamp
                  override var createdAt: Date? = null,
                  override var deliveryStatus: String? = null,
                  override val type: String = MessageType.TEXT) : Message, Parcelable {}

@Parcelize
class ImageMessage(var imagePath: String = "",
                    override var text: String = "",
                    override var sendBy: String = "",
                    @ServerTimestamp
                    override var createdAt: Date? = null,
                    override var deliveryStatus: String? = null,
                    override val type: String = MessageType.IMAGE) : Message, Parcelable {}