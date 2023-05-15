package com.android.chatmeup.ui.screens.homescreen.viewmodel

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.chatmeup.data.datastore.CmuDataStoreRepository
import com.android.chatmeup.data.db.entity.Chat
import com.android.chatmeup.data.db.entity.UserFriend
import com.android.chatmeup.data.db.entity.UserInfo
import com.android.chatmeup.data.db.remote.FirebaseReferenceValueObserver
import com.android.chatmeup.data.db.repository.DatabaseRepository
import com.android.chatmeup.ui.DefaultViewModel
import com.android.chatmeup.util.addNewItem
import com.android.chatmeup.util.convertTwoUserIDs
import com.android.chatmeup.util.updateItemAt
import com.android.chatmeup.data.Result
import com.android.chatmeup.data.db.entity.Message
import com.android.chatmeup.data.db.entity.User
import com.android.chatmeup.data.db.entity.UserNotification
import com.android.chatmeup.data.db.repository.AuthRepository
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.util.removeItem
import com.android.chatmeup.data.model.ChatWithUserInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

enum class HomeEventState{
    INIT,
    LOADING,
    SUCCESS,
    ERROR
}

enum class AddContactEventState{
    DO_NOTHING,
    LOADING,
    ERROR,
    SUCCESS
}

class HomeViewModel @AssistedInject constructor(
    @Assisted("myUserId") private val myUserId: String,
    private val cmuDataStoreRepository: CmuDataStoreRepository
): DefaultViewModel() {
    private val tag = "HomeViewModel"
    val homeEventState = MutableStateFlow(HomeEventState.INIT)

    private val _addContactEventState = MutableStateFlow(AddContactEventState.DO_NOTHING)
    val addContactEventState = _addContactEventState.asStateFlow()

    private val authRepository: AuthRepository = AuthRepository()
    private val dbRepository: DatabaseRepository = DatabaseRepository()
    private val firebaseReferenceObserverList = ArrayList<FirebaseReferenceValueObserver>()

    private val _updatedUserNotification = MutableLiveData<UserInfo>()
    private val _updatedChatWithUserInfo = MutableLiveData<ChatWithUserInfo>()

    val myUpdatedInfo = MutableLiveData<UserInfo>()


    val chatsList = MediatorLiveData<MutableList<ChatWithUserInfo>>()
    val notificationListWithUserInfo = MediatorLiveData<MutableList<UserInfo>>()

    init {
        chatsList.addSource(_updatedChatWithUserInfo) { newChat ->
            val chat = chatsList.value?.find { it.mChat.info.id == newChat.mChat.info.id }
            if (chat == null) {
                chatsList.addNewItem(newChat)
            } else {
                chatsList.updateItemAt(newChat, chatsList.value!!.indexOf(chat))
            }
        }
        this.notificationListWithUserInfo.addSource(_updatedUserNotification) { newNotification ->
            this.notificationListWithUserInfo.addNewItem(newNotification)
        }
        loadAndObserveMyInfo()
        setupChats()
        loadAndObserveNotifications()
    }

    override fun onCleared() {
        super.onCleared()
        firebaseReferenceObserverList.forEach { it.clear() }
    }

    private fun setupChats() {
        loadAndObserveFriends()
    }

    private fun loadAndObserveMyInfo() {
        val observer = FirebaseReferenceValueObserver()
        firebaseReferenceObserverList.add(observer)
        dbRepository.loadAndObserveUserInfo(myUserId, observer) { result: Result<UserInfo> ->
            onResult(myUpdatedInfo, result)
        }
    }

    private fun updateNotification(otherUserInfo: UserInfo, removeOnly: Boolean) {
        val userNotification = this.notificationListWithUserInfo.value?.find {
            it.id == otherUserInfo.id
        }

        if (userNotification != null) {
            if (!removeOnly) {
                dbRepository.updateNewFriend(UserFriend(myUserId), UserFriend(otherUserInfo.id))
                val newChat = Chat().apply {
                    info.id = convertTwoUserIDs(myUserId, otherUserInfo.id)
                    lastMessage = Message(senderID = myUserId, seen = false, text = "Say hello!")
                }
                dbRepository.updateNewChat(newChat)
            }
            dbRepository.removeNotification(myUserId, otherUserInfo.id)
            dbRepository.removeSentRequest(otherUserInfo.id, myUserId)

//            usersInfoList.removeItem(otherUserInfo)
            this.notificationListWithUserInfo.removeItem(userNotification)
        }
    }

    fun acceptNotificationPressed(userInfo: UserInfo) {
        updateNotification(userInfo, false)
    }

    fun declineNotificationPressed(userInfo: UserInfo) {
        updateNotification(userInfo, true)
    }

    private fun loadUserInfo(userFriend: UserFriend) {
        dbRepository.loadUserInfo(userFriend.userID) { result: Result<UserInfo> ->
            onResult(null, result)
            if (result is Result.Success) result.data?.let {
                loadAndObserveChat(it)
            }
        }
    }

    private fun loadUserInfo(userNotification: UserNotification) {
        dbRepository.loadUserInfo(userNotification.userID) { result: Result<UserInfo> ->
            onResult(_updatedUserNotification, result)
        }
    }

    private fun loadAndObserveChat(userInfo: UserInfo) {
        val observer = FirebaseReferenceValueObserver()
        firebaseReferenceObserverList.add(observer)
        if(chatsList.value?.find{it.mUserInfo == userInfo} == null){
            dbRepository.loadAndObserveChat(
                convertTwoUserIDs(myUserId, userInfo.id),
                observer
            ) { result: Result<Chat> ->
                if (result is Result.Success) {
                    _updatedChatWithUserInfo.value =
                        result.data?.let { ChatWithUserInfo(it, userInfo) }
                    result.data?.let { loadAndObserveUserInfo(it, userInfo) }
                } else if (result is Result.Error) {
                    chatsList.value?.let {
                        val newList = mutableListOf<ChatWithUserInfo>().apply { addAll(it) }
                        newList.removeIf { it2 -> result.msg.toString().contains(it2.mUserInfo.id) }
                        chatsList.value = newList
                    }
                }
            }
        }
    }

    private fun loadAndObserveUserInfo(chat: Chat, otherUserInfo: UserInfo){
        val observer = FirebaseReferenceValueObserver()
        firebaseReferenceObserverList.add(observer)
        dbRepository.loadAndObserveUserInfo(
            otherUserInfo.id,
            observer
        ) { result: Result<UserInfo> ->
            if (result is Result.Success) {
                _updatedChatWithUserInfo.value =
                    result.data?.let { ChatWithUserInfo(chat, it) }
            } else if (result is Result.Error) {
                chatsList.value?.let {
                    val newList = mutableListOf<ChatWithUserInfo>().apply { addAll(it) }
                    newList.removeIf { it2 -> result.msg.toString().contains(it2.mUserInfo.id) }
                    chatsList.value = newList
                }
            }
        }
    }

    private fun loadAndObserveNotifications(){
        Timber.tag(tag).d("uidnotifica is $myUserId")
        val observer = FirebaseReferenceValueObserver()
        firebaseReferenceObserverList.add(observer)
        dbRepository.loadAndObserveUserNotifications(myUserId, observer){result ->
            if (result is Result.Success) {
                result.data?.forEach { loadUserInfo(it) }
            }
        }
    }

    private fun loadAndObserveFriends(){
        Timber.tag(tag).d("uidnotifica is $myUserId")
        val observer = FirebaseReferenceValueObserver()
        firebaseReferenceObserverList.add(observer)
        dbRepository.loadAndObserveFriends(myUserId, observer){result ->
            if (result is Result.Success) {
                result.data?.forEach { loadUserInfo(it) }
            }
        }
    }

//    fun onEventTriggered(activity: Activity, events: HomeEvents){
//        when(events){
//            HomeEvents.ChatListEvent -> {
//                homeEventState.value = HomeEventState.CHATS
//            }
//            HomeEvents.ContactListEvent -> {
//                homeEventState.value = HomeEventState.CONTACTS
//            }
//            HomeEvents.MoreEvent -> {
//                homeEventState.value = HomeEventState.MORE
//            }
//        }
//    }

    fun onAddContactEventTriggered(
        event: AddContactEvents,
        context: Context,
        activity: Activity?,
        errorMsg: String = "",
        email: String = "",
    ){
        when(event){
            AddContactEvents.DoNothing -> {
                _addContactEventState.value = AddContactEventState.DO_NOTHING
            }
            AddContactEvents.Loading -> {
                _addContactEventState.value = AddContactEventState.LOADING
                sendFriendRequest(context, activity, email)
            }
            AddContactEvents.Success -> {
                _addContactEventState.value = AddContactEventState.SUCCESS
                Handler(Looper.getMainLooper()).postDelayed({
                    CmuToast.createFancyToast(
                        context,
                        activity,
                        "Add Contact",
                        "Request Sent",
                        CmuToastStyle.SUCCESS,
                        CmuToastDuration.SHORT
                    )
                }, 200)
                onAddContactEventTriggered(
                    event = AddContactEvents.DoNothing,
                    context, activity, errorMsg = "User does not have a ChatMeUp Account"
                )
            }
            AddContactEvents.Error -> {
                _addContactEventState.value = AddContactEventState.ERROR
                Handler(Looper.getMainLooper()).postDelayed({
                    CmuToast.createFancyToast(
                        context,
                        activity,
                        "Add Contact",
                        errorMsg,
                        CmuToastStyle.ERROR,
                        CmuToastDuration.SHORT
                    )
                }, 200)
                onAddContactEventTriggered(
                    event = AddContactEvents.DoNothing,
                    context, activity
                )
            }
        }

    }


    @WorkerThread
    private fun sendFriendRequest(
        context: Context,
        activity: Activity?,
        email: String
    ){
        var uid = ""
        dbRepository.loadUsers { result: Result<MutableList<User>> ->
            onResult(null, result)
            if (result is Result.Success) {
                result.data?.forEach {
                    if (it.info.email == email) {
                        uid = it.info.id
                        return@forEach
                    }
                }
                if (uid.isNotBlank()){
                    Timber.tag(tag).d("This is UID: $uid")
//                    dbRepository.updateNewSentRequest(myUserId.value!!, UserRequest(uid))
                    dbRepository.updateNewNotification(uid, UserNotification(myUserId))
                    onAddContactEventTriggered(
                        AddContactEvents.Success,
                        context, activity
                    )
                }
                else {
                    Timber.tag(tag).d("This is UID: $uid")
                    onAddContactEventTriggered(
                        AddContactEvents.Error,
                        context, activity,
                        errorMsg = "User does not have a ChatMeUp Account"
                    )
                }
            }
            else if(result is Result.Error){
                result.msg?.let {
                    onAddContactEventTriggered(
                        AddContactEvents.Error,
                        context, activity,
                        errorMsg = it
                    )
                }
            }
        }
    }

    fun logout(){
        authRepository.logoutUser()
    }

    sealed class HomeEvents(){
        object ChatListEvent: HomeEvents()
        object ContactListEvent: HomeEvents()
        object MoreEvent: HomeEvents()
    }

    sealed class AddContactEvents(){
        object DoNothing: AddContactEvents()
        object Loading: AddContactEvents()
        object Success: AddContactEvents()
        object Error: AddContactEvents()

    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("myUserId") myUserId: String,
            ): HomeViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            myUserId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return assistedFactory.create(
                    myUserId = myUserId
                ) as T
            }
        }
    }
}