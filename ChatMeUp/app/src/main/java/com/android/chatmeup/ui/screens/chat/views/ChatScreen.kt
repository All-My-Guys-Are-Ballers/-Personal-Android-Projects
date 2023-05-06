package com.android.chatmeup.ui.screens.chat.views

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import com.android.chatmeup.ui.screens.chat.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    context: Context,
    activity: Activity?,
    factory: ChatViewModel.Factory,
    chatId: String,
    userId: String,
    otherUserId: String
) {

}