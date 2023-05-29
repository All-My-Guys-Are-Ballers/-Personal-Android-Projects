package com.android.chatmeup.data.db.room_db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.chatmeup.data.db.room_db.dao.ChatDao
import com.android.chatmeup.data.db.room_db.dao.ContactDao
import com.android.chatmeup.data.db.room_db.dao.MessageDao
import com.android.chatmeup.data.db.room_db.entity.Chat
import com.android.chatmeup.data.db.room_db.entity.Contact
import com.android.chatmeup.data.db.room_db.entity.Message

@Database(
    entities = [Chat::class, Message::class, Contact::class],
    version = 1
)
abstract class ChatMeUpDatabase: RoomDatabase() {
    abstract val chatDao: ChatDao
    abstract val messageDao: MessageDao
    abstract val contactDao: ContactDao
}