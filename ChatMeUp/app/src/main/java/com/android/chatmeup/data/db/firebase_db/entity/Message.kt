package com.android.chatmeup.data.db.firebase_db.entity

import com.google.firebase.database.PropertyName
import java.util.*


data class Message(
    @get:PropertyName("senderID") @set:PropertyName("senderID") var senderID: String = "",
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl") var imageUrl: String = "",
    @get:PropertyName("messageType") @set:PropertyName("messageType") var messageType: String = "TEXT",
    @get:PropertyName("lowQualityThumbnail") @set:PropertyName("lowQualityThumbnail") var lowQualityThumbnail: ByteArray? = null,
    @get:PropertyName("text") @set:PropertyName("text") var text: String = "",
    @get:PropertyName("epochTimeMs") @set:PropertyName("epochTimeMs") var epochTimeMs: Long = Date().time,
    @get:PropertyName("seen") @set:PropertyName("seen") var seen: Boolean = false,
    @get:PropertyName("messageID") @set:PropertyName("messageID") var messageID: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (lowQualityThumbnail != null) {
            if (other.lowQualityThumbnail == null) return false
            if (!lowQualityThumbnail.contentEquals(other.lowQualityThumbnail)) return false
        } else if (other.lowQualityThumbnail != null) return false

        return true
    }

    override fun hashCode(): Int {
        return lowQualityThumbnail?.contentHashCode() ?: 0
    }
}