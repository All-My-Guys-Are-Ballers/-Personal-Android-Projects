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
import com.android.chatmeup.data.db.firebase_db.entity.Chat
import com.android.chatmeup.data.db.firebase_db.entity.ChatInfo
import com.android.chatmeup.data.db.firebase_db.entity.Message
import com.android.chatmeup.data.db.firebase_db.entity.UserInfo
import com.android.chatmeup.data.db.firebase_db.remote.FirebaseReferenceChildObserver
import com.android.chatmeup.data.db.firebase_db.remote.FirebaseReferenceValueObserver
import com.android.chatmeup.data.db.firebase_db.repository.DatabaseRepository
import com.android.chatmeup.data.db.firebase_db.repository.StorageRepository
import com.android.chatmeup.data.db.room_db.ChatMeUpDatabase
import com.android.chatmeup.data.db.room_db.data.MessageStatus
import com.android.chatmeup.ui.DefaultViewModel
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.ui.screens.chat.data.ChatState
import com.android.chatmeup.util.addNewItem
import com.android.chatmeup.util.convertFileToByteArray
import com.android.chatmeup.util.convertFileToLowQualityThumbnail
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewModel @AssistedInject constructor(
    @Assisted("chatId") private val chatID: String,
    @Assisted("myUserId") private val myUserID: String,
    @Assisted("otherUserId") private val otherUserId: String,
    private val cmuDataStoreRepository: CmuDataStoreRepository,
    private val chatMeUpDatabase: ChatMeUpDatabase,
) : DefaultViewModel() {
    private val tag = Companion::class.java.simpleName
    private val dbRepository: DatabaseRepository = DatabaseRepository()
    private val storageRepository = StorageRepository()

    private val _otherUser: MutableLiveData<UserInfo> = MutableLiveData()
    private val _chatInfo: MutableLiveData<ChatInfo> = MutableLiveData()
    private val _addedMessage = MutableLiveData<Message>()
    private val _updatedMessageInfo = MutableLiveData<Message>()

    private val _downloadProgress = MutableStateFlow(0.0)

    private val fbRefMessagesChildObserver = FirebaseReferenceChildObserver()
    private val fbRefUserInfoObserver = FirebaseReferenceValueObserver()
    private val fbRefChatInfoObserver = FirebaseReferenceValueObserver()

    val messagesList = MediatorLiveData<MutableList<Message>>()
    var messagesList1 = emptyFlow<List<com.android.chatmeup.data.db.room_db.entity.Message>>()
    val newMessageText = MutableLiveData("")
    val otherUser: LiveData<UserInfo> = _otherUser
    val chatInfo: LiveData<ChatInfo> = _chatInfo
    val chatState = MutableStateFlow(ChatState.CHAT)
    val downloadProgress = _downloadProgress.asStateFlow()

    val lazyListState = MutableStateFlow(LazyListState())

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    init {
        setupChat()
        loadChatFromDb()
        checkAndUpdateLastMessageSeen()
    }

    override fun onCleared() {
        super.onCleared()
        fbRefMessagesChildObserver.clear()
        fbRefUserInfoObserver.clear()
        fbRefChatInfoObserver.clear()
    }

    private fun checkAndUpdateLastMessageSeen() {
        dbRepository.loadChat(chatID) { result: Result<Chat> ->
            if (result is Result.Success && result.data != null) {
                result.data.let {
                    if (!it.lastMessage.seen && it.lastMessage.senderID != myUserID) {
                        it.lastMessage.seen = true
                        it.info.no_of_unread_messages = 0
                        dbRepository.updateChatLastMessage(chatID, it)
                    }
                }
            }
        }
    }


    private fun checkAndUpdateUnreadMessages(message: Message) {
        dbRepository.loadChat(chatID) { result: Result<Chat> ->
            if (result is Result.Success && result.data != null) {
                result.data.let {
                    val chat = it.apply {
                        it.lastMessage = message
                        it.info.no_of_unread_messages++ }
                    dbRepository.updateChatLastMessage(chatID, chat)
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
        dbRepository.loadAndObserveChatInfo(chatID, fbRefChatInfoObserver){
            onResult(_chatInfo, it)
        }
    }

    private fun loadChatFromDb(){
        ioScope.launch {
            messagesList1 = chatMeUpDatabase.messageDao.getMessagesOrderedByTime(chatID = chatID)
        }
    }

    private fun loadAndObserveNewMessages() {
        messagesList.addSource(_addedMessage) { messagesList.addNewItem(it) }

        dbRepository.loadAndObserveMessagesAdded(
            chatID,
            fbRefMessagesChildObserver
        ) { result: Result<Message> ->
            dbRepository.updateUnreadMessages(chatID, 0)
            if(result is Result.Success){
                onResult(_addedMessage, result)
                result.data?.let{
                    if (it.senderID == otherUserId && !it.seen) {
                        dbRepository.updateSeenMessage(chatID, it.messageID, true)
                    }
                }
            }
        }
    }

    fun sendMessagePressed(
        context: Context,
        activity: Activity?,
        newPhotoURI: Uri?
    ) {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(Date())
        val messageID = chatID+timeStamp
        val lowQualityThumbnail = convertFileToLowQualityThumbnail(context, newPhotoURI)
        val byteArray = convertFileToByteArray(context, newPhotoURI)
        //update database
        ioScope.launch{
            val message = com.android.chatmeup.data.db.room_db.entity.Message(
                messageId = messageID,
                messageText = newMessageText.value!!,
                messageTime = Date().time,
                senderID = myUserID,
                chatID = chatID,
                messageStatus = MessageStatus.UNSENT,
                lowQualityThumbnail = lowQualityThumbnail,
                fileName = if(newPhotoURI != null) chatID+timeStamp else null
            )
            chatMeUpDatabase.messageDao.upsertMessage(message)

            val mutableMessageMap = mutableMapOf(
                "notificationType" to "MESSAGE",
                "messageText" to newMessageText.value!!,
                "messageTime" to Date().time.toString(),
                "senderID" to myUserID,
                "chatID" to chatID,
            )
            if(lowQualityThumbnail != null) {
                mutableMessageMap["lowQualityThumbnail"] = String(lowQualityThumbnail)
            }
            //send notification to device
            Firebase.messaging.send(
                RemoteMessage.Builder("$otherUserId@fcm.googleapis.com")
                    .setData(mutableMessageMap)
                    .setMessageId(messageID)
                    .build()
            )
        }

        //update storage
        byteArray?.let{imageBytes ->
            context.openFileOutput("$messageID.png", Context.MODE_PRIVATE).use {
                it.write(imageBytes)
            }
        }

        if(newPhotoURI == null){
            if (!newMessageText.value.isNullOrBlank()) {
                val newMsg = Message(
                    messageID = messageID,
                    senderID = myUserID,
                    text = newMessageText.value!!,
                    messageType = "TEXT"
                )
                dbRepository.updateNewMessage(chatID, newMsg)
                checkAndUpdateUnreadMessages(newMsg)
                newMessageText.value = ""
            }
        }
        else{
            val msg = Message(
                messageID = messageID,
                senderID = myUserID,
                text = newMessageText.value!!,
            )
            newMessageText.value = ""
            byteArray?.let {
                storageRepository.uploadChatImage(
                    chatID = chatID,
                    byteArray = it
                ){result ->
                    if(result is Result.Success){
                        val newMsg = msg.apply {
                            imageUrl = result.data.toString()
                        }
                        dbRepository.updateNewMessage(chatID, newMsg)
                        checkAndUpdateUnreadMessages(newMsg)
                        newMessageText.value = ""
                    } else if(result is Result.Error){
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
    }

    private fun loadImage(
        context: Context,
        activity: Activity?,
        messageID: String
    ){
        val file = File(context.filesDir, "$messageID.png")
        storageRepository.downloadChatImage(chatID, file){result ->
            when(result){
                is Result.Error -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        CmuToast.createFancyToast(
                            context,
                            activity,
                            "Download Failed",
                            "Unable to download Image",
                            CmuToastStyle.ERROR,
                            CmuToastDuration.SHORT
                        )
                    }, 200)
                }
                Result.Loading -> {
                    // do nothing for now at least
                }
                is Result.Progress -> {
                    _downloadProgress.value = result.double
                }
                is Result.Success -> {
                    //update database
                    ioScope.launch {
                        val message = chatMeUpDatabase.messageDao.getMessage(messageID)
                        chatMeUpDatabase.messageDao.upsertMessage(message.apply { fileName = "$messageID.png" })
                    }
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