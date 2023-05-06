package com.android.chatmeup.ui.screens.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.chatmeup.data.datastore.CmuDataStoreRepository
import com.android.chatmeup.ui.DefaultViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ChatViewModel @AssistedInject constructor(
    @Assisted("chatId") private val chatId: String,
    @Assisted("myUserId") private val myUserId: String,
    @Assisted("otherUserId") private val otherUserId: String,
    private val cmuDataStoreRepository: CmuDataStoreRepository
) : DefaultViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("chatId") chatId: String,
            @Assisted("myUserId") myUserId: String,
            @Assisted("otherUserId") otherUserId: String,
        ): ChatViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            chatId: String,
            myUserId: String,
            otherUserId: String,

        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return assistedFactory.create(
                    chatId, myUserId, otherUserId
                ) as T
            }
        }
    }
}