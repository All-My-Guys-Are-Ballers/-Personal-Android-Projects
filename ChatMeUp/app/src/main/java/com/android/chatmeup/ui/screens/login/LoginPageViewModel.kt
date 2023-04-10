package com.android.chatmeup.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow

enum class LoginStatus{
    INIT,
    INPUT_PHONE_NO,
    INPUT_CODE,
}
class LoginPageViewModel: ViewModel() {
    val loginEventStatus = MutableStateFlow(LoginStatus.INIT)

    fun onEventTriggered(
        event: LoginEvents
    ){
        when(event){
            LoginEvents.InitLoginEvent -> {
                loginEventStatus.value = LoginStatus.INIT
            }
            LoginEvents.InputCodeEvent -> {
                loginEventStatus.value = LoginStatus.INPUT_CODE
            }
            LoginEvents.InputPhoneNoEvent -> {
                loginEventStatus.value = LoginStatus.INPUT_PHONE_NO
            }
        }
    }

    sealed class LoginEvents(){
        object InitLoginEvent: LoginEvents()
        object InputPhoneNoEvent: LoginEvents()
        object InputCodeEvent: LoginEvents()
    }
}