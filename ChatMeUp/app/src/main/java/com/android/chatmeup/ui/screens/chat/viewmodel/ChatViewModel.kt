package com.android.chatmeup.ui.screens.chat.viewmodel

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.chatmeup.data.Result
import com.android.chatmeup.data.datastore.CmuDataStoreRepository
import com.android.chatmeup.data.db.entity.Chat
import com.android.chatmeup.data.db.entity.ChatInfo
import com.android.chatmeup.data.db.entity.Message
import com.android.chatmeup.data.db.entity.UserInfo
import com.android.chatmeup.data.db.remote.FirebaseReferenceChildObserver
import com.android.chatmeup.data.db.remote.FirebaseReferenceValueObserver
import com.android.chatmeup.data.db.repository.DatabaseRepository
import com.android.chatmeup.data.db.repository.StorageRepository
import com.android.chatmeup.ui.DefaultViewModel
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.ui.screens.chat.data.ChatState
import com.android.chatmeup.util.addNewItem
import com.android.chatmeup.util.convertFileToByteArray
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow

class ChatViewModel @AssistedInject constructor(
    @Assisted("chatId") private val chatId: String,
    @Assisted("myUserId") private val myUserId: String,
    @Assisted("otherUserId") private val otherUserId: String,
    private val cmuDataStoreRepository: CmuDataStoreRepository
) : DefaultViewModel() {
    private val dbRepository: DatabaseRepository = DatabaseRepository()
    private val storageRepository = StorageRepository()

    private val _otherUser: MutableLiveData<UserInfo> = MutableLiveData()
    private val _chatInfo: MutableLiveData<ChatInfo> = MutableLiveData()
    private val _addedMessage = MutableLiveData<Message>()

    private val fbRefMessagesChildObserver = FirebaseReferenceChildObserver()
    private val fbRefUserInfoObserver = FirebaseReferenceValueObserver()
    private val fbRefChatInfoObserver = FirebaseReferenceValueObserver()

    val messagesList = MediatorLiveData<MutableList<Message>>()
    val newMessageText = MutableLiveData("")
    val otherUser: LiveData<UserInfo> = _otherUser
    val chatInfo: LiveData<ChatInfo> = _chatInfo
    val chatState = MutableStateFlow(ChatState.CHAT)

    val lazyListState = MutableStateFlow(LazyListState())

    init {
        setupChat()
        checkAndUpdateLastMessageSeen()
    }

    override fun onCleared() {
        super.onCleared()
        fbRefMessagesChildObserver.clear()
        fbRefUserInfoObserver.clear()
        fbRefChatInfoObserver.clear()
    }

    private fun checkAndUpdateLastMessageSeen() {
        dbRepository.loadChat(chatId) { result: Result<Chat> ->
            if (result is Result.Success && result.data != null) {
                result.data.let {
                    if (!it.lastMessage.seen && it.lastMessage.senderID != myUserId) {
                        it.lastMessage.seen = true
                        it.info.no_of_unread_messages = 0
                        dbRepository.updateChatLastMessage(chatId, it)
                    }
                }
            }
        }
    }


    private fun checkAndUpdateUnreadMessages(message: Message) {
        dbRepository.loadChat(chatId) { result: Result<Chat> ->
            if (result is Result.Success && result.data != null) {
                result.data.let {
                    val chat = it.apply {
                        it.lastMessage = message
                        it.info.no_of_unread_messages++ }
                    dbRepository.updateChatLastMessage(chatId, chat)
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
        dbRepository.loadAndObserveChatInfo(chatId, fbRefChatInfoObserver){
            onResult(_chatInfo, it)
        }
    }

    private fun loadAndObserveNewMessages() {
        messagesList.addSource(_addedMessage) { messagesList.addNewItem(it) }

        dbRepository.loadAndObserveMessagesAdded(
            chatId,
            fbRefMessagesChildObserver
        ) { result: Result<Message> ->
            onResult(_addedMessage, result)
            dbRepository.updateUnreadMessages(chatId, 0)
        }
    }

    fun sendMessagePressed(
        context: Context,
        activity: Activity?,
        newPhotoURI: Uri?
    ) {
        if(newPhotoURI == null){
            if (!newMessageText.value.isNullOrBlank()) {
                val newMsg = Message(senderID = myUserId, text = newMessageText.value!!)
                dbRepository.updateNewMessage(chatId, newMsg)
                checkAndUpdateUnreadMessages(newMsg)
                newMessageText.value = ""
            }
        }
        else{
            val msg = Message(
                senderID = myUserId,
                text = newMessageText.value!!,
            )
            newMessageText.value = ""
            storageRepository.uploadChatImage(
                chatID = chatId,
                byteArray = convertFileToByteArray(context, newPhotoURI)
            ){result ->
                if(result is Result.Success){
                    val newMsg = msg.apply {
                        imageUrl = result.data.toString()
                    }
                    dbRepository.updateNewMessage(chatId, newMsg)
                    checkAndUpdateUnreadMessages(newMsg)
                    newMessageText.value = ""
                }
                else if(result is Result.Error){
                    Handler(Looper.getMainLooper()).postDelayed({
                        CmuToast.createFancyToast(
                            context,
                            activity,
                            "Upload Failed",
                            "Unable to upload Image",
                            CmuToastStyle.ERROR,
                            CmuToastDuration.SHORT
                        )
                    }, 200)
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