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
import com.android.chatmeup.data.db.firebase_db.repository.StorageRepository
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
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

    private val storageRepository = StorageRepository()

    private val loginCredentials = MutableStateFlow("")

    private val _loadingMsg = MutableStateFlow("Loading...")
    val loadingMsg = _loadingMsg.asStateFlow()

    private var myUserID: String? = null
    private var token: String = ""

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    init {
        Firebase.messaging.token.addOnCompleteListener {
            token = it.result
        }
    }

    fun onEventTriggered(
        event: LoginEvents,
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
                    token = token,
                    activity = event.activity
                ) {
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
                                        senderID = message.senderID,
                                        messageStatus = MessageStatus.UNSENT,
                                        lowQualityThumbnail = message.lowQualityThumbnail.toByteArray(),
                                    )
                                )
                            }
                        }
                    }
                }

                is Result.Progress -> {
                    //do nothing
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
                    is Result.Error -> {
                        Timber.tag(tag).d("Unable to load chat errorMsg: ${chatResult.msg}")
                    }
                    Result.Loading -> {
                        //do nothing for now
                    }
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

                    is Result.Progress -> {
                        //do nothing
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
        Firebase.auth.currentUser?.uid?.let {
            dbRepository.loadUserInfo(it){result ->
                when(result){
                    is Result.Error -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            CmuToast.createFancyToast(
                                context,
                                activity,
                                "Download Failed",
                                "Unable to load User Info",
                                CmuToastStyle.ERROR,
                                CmuToastDuration.SHORT
                            )
                        }, 200)
                    }
                    Result.Loading -> {}
                    is Result.Progress -> {}
                    is Result.Success -> {
                        result.data?.let { it1 ->
                            loadProfileImage(
                                context,
                                activity,
                                it1.id,
                                it1.displayName,
                                it1.aboutStr,
                                isMine = true,
                                email = it1.email
                            )
                        }
                    }
                }
            }
        }

        myUserID?.let {
            dbRepository.loadFriends(it){result ->
                when(result){
                    is Result.Success -> {
                        result.data?.let {contactList ->
                            contactList.forEach { contact ->
                                dbRepository.loadUserInfo(contact.userID){infoResult ->
                                    when(infoResult){
                                        is Result.Error -> {
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                CmuToast.createFancyToast(
                                                    context,
                                                    activity,
                                                    "Download Failed",
                                                    "Unable to load User Info",
                                                    CmuToastStyle.ERROR,
                                                    CmuToastDuration.SHORT
                                                )
                                            }, 200)
                                        }
                                        Result.Loading -> {}
                                        is Result.Progress -> {}
                                        is Result.Success -> {
                                            infoResult.data?.let{info ->
                                                loadProfileImage(
                                                    context,
                                                    activity,
                                                    contact.userID,
                                                    displayName = info.displayName,
                                                    status = info.aboutStr,
                                                    false,
                                                    email = info.email,
                                                )
                                            }
                                        }
                                    }
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

                    is Result.Progress -> {
                        //do nothing
                    }
                }
            }
        }?: {
            onEventTriggered(LoginEvents.ErrorEvent(activity, context, "UserId is null"))
        }
    }

    private fun loadProfileImage(
        context: Context,
        activity: Activity?,
        userID: String,
        displayName: String,
        status: String,
        isMine: Boolean,
        email: String,
    ){
        val localPath = "profile_photos/${if(isMine) "my_profile" else userID}.png"
        storageRepository.downloadProfileImage(userID, context, localPath) { result ->
            when (result) {
                is Result.Error -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        CmuToast.createFancyToast(
                            context,
                            activity,
                            "Download Failed",
                            "Unable to download Profile Image",
                            CmuToastStyle.ERROR,
                            CmuToastDuration.SHORT
                        )
                    }, 200)
                    Timber.tag(tag).d("Unable to download profile image for $displayName")
                }

                Result.Loading -> {
                    // do nothing for now at least
                    Timber.tag(tag).d("Saving contact loading for $displayName")
                }

                is Result.Progress -> {
//                    _downloadProgress.value = result.double
                    Timber.tag(tag).d("Saving profile Image Progress ${result.double} for $displayName")
                }

                is Result.Success -> {
                    Timber.tag(tag).d("Saving profile Success ${result.msg} for $displayName")
                    //update database
                    ioScope.launch{
                        val contact = Contact(
                            userID,
                            displayName,
                            email,
                            status,
                            localPath,
                            "user_photos/$userID/profile_image"
                        )
                        chatMeUpDatabase.contactDao.upsertContact(contact)
                        Timber.tag(tag).d("Contact Saved for $displayName")
                    }
                }
            }
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
        token: String,
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
                    dbRepository.updateFCMToken(it.uid, token)
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

