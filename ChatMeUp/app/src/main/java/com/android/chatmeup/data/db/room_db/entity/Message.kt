package com.android.chatmeup.data.db.room_db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.android.chatmeup.data.db.room_db.data.MessageStatus
import com.android.chatmeup.data.db.room_db.data.MessageStatusEnumConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


@Entity
@TypeConverters(MessageStatusEnumConverter::class)
data class Message(
    @PrimaryKey
    val messageId: String, //chatId + messageId
    val messageText: String,
    val messageTime: Long,
    val messageType: String,
    val senderID: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val lowQualityThumbnail: ByteArray? = null,
    var localFilePath: String? = null,
    var serverFilePath: String? = null,
    val messageStatus: MessageStatus,
    val chatID: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (!lowQualityThumbnail.contentEquals(other.lowQualityThumbnail)) return false

        return true
    }

    override fun hashCode(): Int {
        return lowQualityThumbnail.contentHashCode()
    }
}

val gson = Gson()

//convert a data class to a map
fun <T> T.serializeToMap(): Map<String, Any> {
    return convert()
}

//convert a map to a data class
inline fun <reified T> Map<String, Any>.toDataClass(): T {
    return convert()
}

//convert an object of type I to type O
inline fun <I, reified O> I.convert(): O {
    val json = gson.toJson(this)
    return gson.fromJson(json, object : TypeToken<O>() {}.type)
}

