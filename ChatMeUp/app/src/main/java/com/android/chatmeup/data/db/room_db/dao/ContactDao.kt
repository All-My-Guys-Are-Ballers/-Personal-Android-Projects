package com.android.chatmeup.data.db.room_db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.android.chatmeup.data.db.room_db.entity.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Upsert
    fun upsertContact(contact: Contact)

    @Query("SELECT * FROM contact")
    fun getContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contact WHERE :userID == userID")
    fun getContact(userID: String): Flow<Contact>
    @Query("SELECT EXISTS(SELECT 1 FROM contact WHERE :userID = userID)")
    fun contactExists(userID: String): Boolean

    @Delete
    fun deleteContact(contact: Contact)
}