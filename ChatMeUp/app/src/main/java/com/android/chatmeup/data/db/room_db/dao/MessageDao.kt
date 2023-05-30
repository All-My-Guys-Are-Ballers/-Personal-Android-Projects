package com.android.chatmeup.data.db.room_db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.android.chatmeup.data.db.room_db.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao{
    @Upsert
    fun upsertMessage(message: Message)

    @Delete
    fun deleteMessage(message: Message)

    @Query("SELECT * FROM message WHERE :messageID = messageId")
    fun getMessage(messageID: String): Message

    @Query("SELECT * FROM message WHERE :chatID = chatID ORDER BY messageTime DESC")
    fun getMessagesOrderedByTime(chatID: String): Flow<List<Message>>

    @Query("SELECT * FROM message WHERE messageText LIKE :searchText ORDER BY messageTime DESC")
    fun searchMessagesOrderedByTime(searchText: String): Flow<List<Message>>
}
