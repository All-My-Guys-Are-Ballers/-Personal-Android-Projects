package com.android.chatmeup.ui.screens.homescreen

import androidx.lifecycle.ViewModel
import com.android.chatmeup.ui.screens.loginscreen.LoginStatus
import kotlinx.coroutines.flow.MutableStateFlow

enum class HomeEventState{
    CHATS,
    CONTACTS,
    MORE
}

class HomeViewModel: ViewModel() {
    val homeEventState = MutableStateFlow(HomeEventState.CHATS)

    fun onEventTriggered(events: HomeEvents){
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
}