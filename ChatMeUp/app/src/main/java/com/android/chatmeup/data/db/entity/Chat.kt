package com.android.chatmeup.data.db.entity

import com.google.firebase.database.PropertyName


data class Chat(
    @get:PropertyName("lastMessage") @set:PropertyName("lastMessage") var lastMessage: Message = Message(),
    @get:PropertyName("info") @set:PropertyName("info") var info: ChatInfo = ChatInfo()
)

data class ChatInfo(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("no_of_unread_messages_for_first_user") @set:PropertyName("no_of_unread_messages_for_first_user") var no_of_unread_messages_for_first_user: Int = 0,
    @get:PropertyName("no_of_unread_messages_for_second_user") @set:PropertyName("no_of_unread_messages_for_second_user") var no_of_unread_messages_for_second_user: Int = 0
)