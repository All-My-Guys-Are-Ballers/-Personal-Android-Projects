package com.android.chatmeup.data.db.room_db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.android.chatmeup.data.db.room_db.entity.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao{
    @Upsert
    fun upsertChat(chat: Chat)

    @Query("SELECT * FROM chat WHERE :chatId = id")
    fun getChat(chatId: String): Chat

    @Delete
    fun deleteChat(chat: Chat)

    @Query("SELECT * FROM chat ORDER BY lastMessageTime DESC")
    fun getChatsOrderedByTime(): Flow<List<Chat>>

    @Query("SELECT * FROM chat WHERE lastMessageText LIKE :searchText ORDER BY lastMessageTime DESC")
    fun searchChatsOrderedByTime(searchText: String): Flow<List<Chat>>
}
