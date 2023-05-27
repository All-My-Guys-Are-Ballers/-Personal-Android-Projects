package com.android.chatmeup.data.db.room_db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.chatmeup.data.db.room_db.dao.ChatDao
import com.android.chatmeup.data.db.room_db.entity.Chat

@Database(
    entities = [Chat::class],
    version = 1
)
abstract class ChatMeUpDatabase: RoomDatabase() {
    abstract val chatDao: ChatDao
}