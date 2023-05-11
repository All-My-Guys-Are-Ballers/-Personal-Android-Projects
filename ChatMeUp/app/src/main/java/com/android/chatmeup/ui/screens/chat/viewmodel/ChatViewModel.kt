package com.android.chatmeup.ui.screens.chat.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.chatmeup.data.Result
import com.android.chatmeup.data.datastore.CmuDataStoreRepository
import com.android.chatmeup.data.db.entity.Chat
import com.android.chatmeup.data.db.entity.Message
import com.android.chatmeup.data.db.entity.User
import com.android.chatmeup.data.db.entity.UserInfo
import com.android.chatmeup.data.db.entity.UserNotification
import com.android.chatmeup.data.db.remote.FirebaseReferenceChildObserver
import com.android.chatmeup.data.db.remote.FirebaseReferenceValueObserver
import com.android.chatmeup.data.db.repository.DatabaseRepository
import com.android.chatmeup.ui.DefaultViewModel
import com.android.chatmeup.util.addNewItem
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ChatViewModel @AssistedInject constructor(
    @Assisted("chatId") private val chatId: String,
    @Assisted("myUserId") private val myUserId: String,
    @Assisted("otherUserId") private val otherUserId: String,
    private val cmuDataStoreRepository: CmuDataStoreRepository
) : DefaultViewModel() {
    private val dbRepository: DatabaseRepository = DatabaseRepository()

    private val _otherUser: MutableLiveData<UserInfo> = MutableLiveData()
    private val _addedMessage = MutableLiveData<Message>()

    private val fbRefMessagesChildObserver = FirebaseReferenceChildObserver()
    private val fbRefUserInfoObserver = FirebaseReferenceValueObserver()

    val messagesList = MediatorLiveData<MutableList<Message>>()
    val newMessageText = MutableLiveData<String>()
    val otherUser: LiveData<UserInfo> = _otherUser

    val lazyListState = MutableStateFlow(LazyListState())

    init {
        setupChat()
        checkAndUpdateLastMessageSeen()
    }

    override fun onCleared() {
        super.onCleared()
        fbRefMessagesChildObserver.clear()
        fbRefUserInfoObserver.clear()
    }

    private fun checkAndUpdateLastMessageSeen() {
        dbRepository.loadChat(chatId) { result: Result<Chat> ->
            if (result is Result.Success && result.data != null) {
                result.data.lastMessage.let {
                    if (!it.seen && it.senderID != myUserId) {
                        it.seen = true
                        dbRepository.updateChatLastMessage(chatId, it)
                    }
                }
            }
        }
    }

    private fun setupChat() {
        dbRepository.loadAndObserveUserInfo(otherUserId, fbRefUserInfoObserver) { result: Result<UserInfo> ->
            onResult(_otherUser, result)
            if (result is Result.Success && !fbRefMessagesChildObserver.isObserving()) {
                loadAndObserveNewMessages()
            }
        }
    }

    private fun loadAndObserveNewMessages() {
        messagesList.addSource(_addedMessage) { messagesList.addNewItem(it) }

        dbRepository.loadAndObserveMessagesAdded(
            chatId,
            fbRefMessagesChildObserver
        ) { result: Result<Message> ->
            onResult(_addedMessage, result)
        }
    }

    fun sendMessagePressed() {
        if (!newMessageText.value.isNullOrBlank()) {
            val newMsg = Message(myUserId, newMessageText.value!!)
            dbRepository.updateNewMessage(chatId, newMsg)
            dbRepository.updateChatLastMessage(chatId, newMsg)
            newMessageText.value = null
            viewModelScope.launch{
                if(lazyListState.value.layoutInfo.totalItemsCount>=1){
                    lazyListState.value.scrollToItem(lazyListState.value.layoutInfo.totalItemsCount - 1)
                }
            }
        }
    }
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