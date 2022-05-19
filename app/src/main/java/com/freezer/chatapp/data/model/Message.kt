package com.freezer.chatapp.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
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

class TextMessage(override var text: String = "",
                  override var sendBy: String = "",
                  @ServerTimestamp
                  override var createdAt: Date? = null,
                  override var deliveryStatus: String? = null,
                  override val type: String = MessageType.TEXT) : Message {}

class ImageMessage(var imagePath: String = "",
                        override var text: String = "",
                        override var sendBy: String = "",
                        @ServerTimestamp
                        override var createdAt: Date? = null,
                        override var deliveryStatus: String?,
                        override val type: String = MessageType.IMAGE) : Message {}