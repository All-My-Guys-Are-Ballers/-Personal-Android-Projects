package com.android.chatmeup.util

import com.android.chatmeup.data.db.firebase_db.entity.Message
import com.android.chatmeup.data.db.room_db.data.MessageStatus

fun firebaseMessageToRoomMessage(message: Message): com.android.chatmeup.data.db.room_db.entity.Message{
    return com.android.chatmeup.data.db.room_db.entity.Message(
        chatID = convertTwoUserIDs(message.senderID, message.receiverID),
        messageStatus = MessageStatus.DELIVERED,
        messageId = message.messageID,
        messageTime = message.epochTimeMs,
        messageText = message.text,
        lowQualityThumbnail = message.lowQualityThumbnail.toByteArray(),
        senderID = message.senderID,
        localFilePath = null,
        serverFilePath = message.imageUrl,
        messageType = message.messageType
    )
}