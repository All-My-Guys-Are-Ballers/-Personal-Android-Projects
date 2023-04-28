package com.android.chatmeup.ui.screens.homescreen

import android.app.Activity
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
import com.android.chatmeup.ui.screens.loginscreen.LoginStatus
import com.android.chatmeup.util.addNewItem
import com.android.chatmeup.util.convertTwoUserIDs
import com.android.chatmeup.util.updateItemAt
import com.fredrikbogg.android_chat_app.data.Result
import com.fredrikbogg.android_chat_app.data.model.ChatWithUserInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class HomeEventState{
    CHATS,
    CONTACTS,
    MORE
}

@HiltViewModel
class HomeViewModel @Inject constructor(cmuDataStoreRepository: CmuDataStoreRepository): DefaultViewModel() {
    val homeEventState = MutableStateFlow(HomeEventState.CHATS)

    private var cmuDataStoreRepository: CmuDataStoreRepository? = null
    private val repository: DatabaseRepository = DatabaseRepository()
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
        cmuDataStoreRepository?.getUserId()?.collect { state ->
            withContext(Dispatchers.IO) {
                myUserId = state
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseReferenceObserverList.forEach { it.clear() }
    }

    private fun setupChats() {
        loadFriends()
    }

    private fun loadFriends() {
        repository.loadFriends(myUserId) { result: Result<List<UserFriend>> ->
            onResult(null, result)
            if (result is Result.Success) result.data?.forEach { loadUserInfo(it) }
        }
    }

    private fun loadUserInfo(userFriend: UserFriend) {
        repository.loadUserInfo(userFriend.userID) { result: Result<UserInfo> ->
            onResult(null, result)
            if (result is Result.Success) result.data?.let { loadAndObserveChat(it) }
        }
    }

    private fun loadAndObserveChat(userInfo: UserInfo) {
        val observer = FirebaseReferenceValueObserver()
        firebaseReferenceObserverList.add(observer)
        repository.loadAndObserveChat(convertTwoUserIDs(myUserId, userInfo.id), observer) { result: Result<Chat> ->
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

    sealed class HomeEvents(){
        object ChatListEvent: HomeEvents()
        object ContactListEvent: HomeEvents()
        object MoreEvent: HomeEvents()
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