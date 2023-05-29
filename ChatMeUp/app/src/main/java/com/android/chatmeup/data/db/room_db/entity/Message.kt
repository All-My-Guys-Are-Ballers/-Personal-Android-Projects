package com.android.chatmeup.data.db.room_db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.android.chatmeup.data.db.room_db.data.MessageStatus
import com.android.chatmeup.data.db.room_db.data.MessageStatusEnumConverter


@Entity
@TypeConverters(MessageStatusEnumConverter::class)
data class Message(
    @PrimaryKey
    val messageId: String, //chatId + messageId
    val messageText: String,
    val messageTime: Long,
    val senderId: String,
    val messageStatus: MessageStatus,
    val chatID: String,
)
