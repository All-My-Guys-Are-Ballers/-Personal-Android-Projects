package com.android.chatmeup.ui.screens.loginscreen

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.chatmeup.data.Result
import com.android.chatmeup.data.datastore.CmuDataStoreRepository
import com.android.chatmeup.data.db.firebase_db.repository.AuthRepository
import com.android.chatmeup.data.db.firebase_db.repository.DatabaseRepository
import com.android.chatmeup.data.db.room_db.ChatMeUpDatabase
import com.android.chatmeup.data.db.room_db.data.MessageStatus
import com.android.chatmeup.data.db.room_db.entity.Chat
import com.android.chatmeup.data.db.room_db.entity.Contact
import com.android.chatmeup.data.db.room_db.entity.Message
import com.android.chatmeup.ui.DefaultViewModel
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.util.convertTwoUserIDs
import com.fredrikbogg.android_chat_app.data.model.Login
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

enum class LoginStatus{
    INIT,
    LOADING,
    LOADING_DATA,
    DONE,
    ERROR,
    ERROR_LOADING_DATA
}

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val cmuDataStoreRepository: CmuDataStoreRepository,
    private val chatMeUpDatabase: ChatMeUpDatabase,
) : DefaultViewModel() {
    private val tag: String = "LoginScreenViewModel"

    val loginEventStatus = MutableStateFlow(LoginStatus.INIT)

    private val authRepository = AuthRepository()

    private val dbRepository = DatabaseRepository()

    private val loginCredentials = MutableStateFlow("")

    private val _loadingMsg = MutableStateFlow("Loading...")
    val loadingMsg = _loadingMsg.asStateFlow()

    private var myUserID: String? = null

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    fun onEventTriggered(
        event: LoginScreenViewModel.LoginEvents,
    ) {
        when (event) {
            LoginEvents.InitLoginEvent -> {
                loginEventStatus.value = LoginStatus.INIT
            }

            is LoginEvents.LoadingEvent -> {
                loginEventStatus.value = LoginStatus.LOADING
                login(
                    email = event.email,
                    password = event.password,
                    context = event.context,
                    activity = event.activity,
                    onDataLoaded = {
                        onEventTriggered(
                            event = LoginEvents.LoadAllData(
                                event.context,
                                event.activity,
                                event.onDataLoaded
                            ),
                        )
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            myUserID?.let { dbRepository.updateFCMToken(it, task.result) }
                        }
                    }
                )
            }

            is LoginEvents.ErrorEvent -> {
                loginEventStatus.value = LoginStatus.ERROR
                Handler(Looper.getMainLooper()).postDelayed({
                    CmuToast.createFancyToast(
                        event.context,
                        event.activity,
                        "Login Error",
                        event.errorMsg ?: "Unknown Error, Please contact Support",
                        CmuToastStyle.ERROR,
                        CmuToastDuration.SHORT
                    )
                }, 200)
                Timber.tag(tag).d("Login Error ${event.errorMsg}")

//                onEventTriggered(
//                    LoginEvents.InitLoginEvent
//                )
            }

            is LoginEvents.DoneEvent -> {
                loginEventStatus.value = LoginStatus.DONE
                try {
                    saveUserId(event.myUserId)
                } catch (e: Exception) {
                    Timber.tag(tag).d("Error: $e")
                }
            }

            is LoginEvents.LoadAllData -> {
                loginEventStatus.value = LoginStatus.LOADING_DATA
                loadAllData(
                    event.context,
                    event.activity,
                    event.onDataLoaded
                )
            }

            is LoginEvents.ErrorLoadingData -> TODO()
        }
    }

    private fun saveUserId(value: String) = viewModelScope.launch {
        cmuDataStoreRepository.saveUserId(value)
    }

    private fun loadMessages(chatID: String){
        dbRepository.loadMessages(chatID){result ->
            when(result){
                is Result.Error -> {
                    Timber.tag(tag).e("Unable to load Messages for $chatID")
                }
                Result.Loading -> {
                    Timber.tag(tag).d("Loading Messages for $chatID")
                }
                is Result.Success -> {
                    result.data?.let{messageList ->
                        messageList.forEach{message ->
                            ioScope.launch{
                                chatMeUpDatabase.messageDao.upsertMessage(
                                    Message(
                                        messageId = chatID + message.messageID,
                                        chatID = chatID,
                                        messageText = message.text,
                                        messageTime = message.epochTimeMs,
                                        senderId = message.senderID,
                                        messageStatus = MessageStatus.UNSENT
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadChat(otherUserID: String) {
        try {
            dbRepository.loadChat(
                convertTwoUserIDs(
                    otherUserID,
                    myUserID!!
                )
            ) { chatResult ->
                when (chatResult) {
                    is Result.Error -> TODO()
                    Result.Loading -> TODO()
                    is Result.Success -> {
                        chatResult.data?.let{chat ->
                            ioScope.launch{
                                chatMeUpDatabase.chatDao.upsertChat(
                                    Chat(
                                        id = chat.info.id,
                                        no_of_unread_messages = chat.info.no_of_unread_messages,
                                        lastMessageTime = chat.lastMessage.epochTimeMs,
                                        lastMessageText = chat.lastMessage.text,
                                        messageType = enumValueOf(chat.lastMessage.messageType),
                                        lastMessageSenderID = chat.lastMessage.senderID
                                    )
                                )
                            }
                            loadMessages(chatID = chat.info.id)
                        } ?: {
                            Timber.tag(tag).e("Unable to load Chat for ${
                                convertTwoUserIDs(
                                    otherUserID,
                                    myUserID!!)
                            }")
                        }
                    }
                }
            }
        } catch (e: NullPointerException) {
            Timber.tag(tag).e("myUserId is null $e")
        }
        catch (e: Exception) {
            // it should never reach these
            Timber.tag(tag).e("Unknown Error $e")
        }
    }

    private fun loadAllData(
        context: Context,
        activity: Activity?,
        onDataLoaded: () -> Unit
    ){
        myUserID?.let {
            dbRepository.loadFriends(it){result ->
                when(result){
                    is Result.Success -> {
                        result.data?.let {contactList ->
                            contactList.forEach { contact ->
                                ioScope.launch{
                                    chatMeUpDatabase.contactDao.upsertContact(Contact(contact.userID))
                                }
                                loadChat(otherUserID = contact.userID)
                            }
                        }
                        onEventTriggered(LoginEvents.DoneEvent(it))
                        onDataLoaded()
                    }
                    is Result.Error -> {
                        onEventTriggered(
                            event = LoginEvents.ErrorEvent(activity, context, result.msg)
                        )
                    }
                    Result.Loading -> {
                        loginEventStatus.value = LoginStatus.LOADING_DATA
                        _loadingMsg.value = "Loading Chats"
                    }
                }
            }
        }?: {
            onEventTriggered(LoginEvents.ErrorEvent(activity, context, "UserId is null"))
        }
    }

    @WorkerThread
    private fun saveLoginCredentials(value: String) = viewModelScope.launch(Dispatchers.IO) {
        cmuDataStoreRepository.saveLoginCredentials(value)
    }

    @WorkerThread
    fun getLoginCredentials() = viewModelScope.launch (
        Dispatchers.IO) {
        cmuDataStoreRepository.getLoginCredentials().collect { state ->
            withContext(Dispatchers.IO) {
                loginCredentials.value = state
            }
        }
    }

    private fun login(
        email: String,
        password: String,
        activity: Activity?,
        context: Context,
        onDataLoaded: () -> Unit
    ) {
        val login = Login(email, password)

        authRepository.loginUser(login) { result: Result<FirebaseUser> ->
            onResult(null, result)
            if (result is Result.Success) {
//                saveUserId(result.data?.uid)
                result.data?.let{
                    myUserID = it.uid
                    onEventTriggered(
                        event = LoginEvents.LoadAllData(
                            context = context,
                            activity = activity,
                            onDataLoaded = onDataLoaded
                        )
                    )
                }
            }
            else if (result is Result.Error) {
                onEventTriggered(
                    event = LoginEvents.ErrorEvent(
                        context = context,
                        activity = activity,
                        errorMsg = result.msg
                    ),
                )
            }
            else if(result is Result.Loading) {
                Timber.tag(tag).d("Logging In")
            }
        }
    }



    sealed class LoginEvents(){
        object InitLoginEvent: LoginEvents()
        data class LoadingEvent(
            val activity: Activity?,
            val context: Context,
            val email: String,
            val password: String ,
            val onDataLoaded : () -> Unit
        ): LoginEvents()
        data class ErrorEvent(
            val activity: Activity?,
            val context: Context,
            val errorMsg : String?
        ): LoginEvents()
        data class DoneEvent(
            val myUserId: String,
            ): LoginEvents()
        data class LoadAllData(
            val context: Context,
            val activity: Activity?,
            val onDataLoaded: () -> Unit
        ) : LoginEvents()
        data class ErrorLoadingData(
            val context: Context,
            val activity: Activity?,
            val errorMsg: String?
        ) : LoginEvents()
    }

    interface Factory {
        fun create(
            cmuDataStoreRepository: CmuDataStoreRepository,
        ): LoginScreenViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            cmuDataStoreRepository: CmuDataStoreRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return assistedFactory.create(
                    cmuDataStoreRepository
                ) as T
            }
        }
    }
}

