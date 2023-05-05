package com.android.chatmeup.ui.screens.homescreen

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread
import androidx.lifecycle.*
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
import com.android.chatmeup.data.db.entity.User
import com.android.chatmeup.data.db.entity.UserNotification
import com.android.chatmeup.data.db.entity.UserRequest
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.fredrikbogg.android_chat_app.data.model.ChatWithUserInfo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

enum class HomeEventState{
    CHATS,
    CONTACTS,
    MORE
}

enum class AddContactEventState{
    DO_NOTHING,
    LOADING,
    ERROR,
    SUCCESS
}

@HiltViewModel
class HomeViewModel @Inject constructor(cmuDataStoreRepository: CmuDataStoreRepository): DefaultViewModel() {
    private val tag = "HomeViewModel"
    val homeEventState = MutableStateFlow(HomeEventState.CHATS)

    private val _addContactEventState = MutableStateFlow(AddContactEventState.DO_NOTHING)
    val addContactEventState = _addContactEventState.asStateFlow()

    private var cmuDataStoreRepository: CmuDataStoreRepository? = null
    private val dbRepository: DatabaseRepository = DatabaseRepository()
    private val firebaseReferenceObserverList = ArrayList<FirebaseReferenceValueObserver>()
    private val _updatedChatWithUserInfo = MutableLiveData<ChatWithUserInfo>()

    private var myUserId: String = ""

    val chatsList = MediatorLiveData<MutableList<ChatWithUserInfo>>()

    init {
        this.cmuDataStoreRepository = cmuDataStoreRepository
        getUserId()
        chatsList.addSource(_updatedChatWithUserInfo) { newChat ->
            val chat = chatsList.value?.find { it.mChat.info.id == newChat.mChat.info.id }
            if (chat == null) {
                chatsList.addNewItem(newChat)
            } else {
                chatsList.updateItemAt(newChat, chatsList.value!!.indexOf(chat))
            }
        }
        setupChats()
    }

    @WorkerThread
    private fun getUserId() = viewModelScope.launch (
        Dispatchers.IO) {
        myUserId = Firebase.auth.currentUser?.uid.toString()
    }

    override fun onCleared() {
        super.onCleared()
        firebaseReferenceObserverList.forEach { it.clear() }
    }

    private fun setupChats() {
        loadFriends()
    }

    private fun loadFriends() {
        dbRepository.loadFriends(myUserId) { result: Result<List<UserFriend>> ->
            onResult(null, result)
            if (result is Result.Success) result.data?.forEach { loadUserInfo(it) }
        }
    }

    private fun loadUserInfo(userFriend: UserFriend) {
        dbRepository.loadUserInfo(userFriend.userID) { result: Result<UserInfo> ->
            onResult(null, result)
            if (result is Result.Success) result.data?.let { loadAndObserveChat(it) }
        }
    }

    private fun loadAndObserveChat(userInfo: UserInfo) {
        val observer = FirebaseReferenceValueObserver()
        firebaseReferenceObserverList.add(observer)
        dbRepository.loadAndObserveChat(convertTwoUserIDs(myUserId, userInfo.id), observer) { result: Result<Chat> ->
            if (result is Result.Success) {
                _updatedChatWithUserInfo.value = result.data?.let { ChatWithUserInfo(it, userInfo) }
            } else if (result is Result.Error) {
                chatsList.value?.let {
                    val newList = mutableListOf<ChatWithUserInfo>().apply { addAll(it) }
                    newList.removeIf { it2 -> result.msg.toString().contains(it2.mUserInfo.id) }
                    chatsList.value = newList
                }
            }
        }
    }

    fun onEventTriggered(activity: Activity, events: HomeEvents){
        when(events){
            HomeEvents.ChatListEvent -> {
                homeEventState.value = HomeEventState.CHATS
            }
            HomeEvents.ContactListEvent -> {
                homeEventState.value = HomeEventState.CONTACTS
            }
            HomeEvents.MoreEvent -> {
                homeEventState.value = HomeEventState.MORE
            }
        }
    }

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
                    dbRepository.updateNewSentRequest(myUserId, UserRequest(uid))
                    dbRepository.updateNewNotification(uid, UserNotification(myUserId))
                    onAddContactEventTriggered(
                        AddContactEvents.Success,
                        context, activity, errorMsg = "User does not have a ChatMeUp Account"
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
                onAddContactEventTriggered(
                    AddContactEvents.Error,
                    context, activity,
                    errorMsg = "Unable to connect"
                )
            }
        }
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

//    @AssistedFactory
//    interface Factory {
//        fun create(
//        ): HomeViewModel
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    companion object {
//        fun provideFactory(
//            assistedFactory: Factory,
//        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
//            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
//                return assistedFactory.create(
//                ) as T
//            }
//        }
//    }
}