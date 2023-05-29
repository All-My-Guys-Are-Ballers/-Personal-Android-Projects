package com.android.chatmeup.data.db.room_db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.android.chatmeup.data.db.room_db.data.MessageType
import com.android.chatmeup.data.db.room_db.data.MessageTypeEnumConverter

@Entity
@TypeConverters(MessageTypeEnumConverter::class)
data class Chat(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val no_of_unread_messages: Int,
    val lastMessageText: String,
    val lastMessageTime: Long,
    val messageType: MessageType,
    val lastMessageSenderID: String
)
