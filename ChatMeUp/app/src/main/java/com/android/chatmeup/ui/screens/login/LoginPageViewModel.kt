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
    val loginEventStatus = MutableStateFlow<LoginStatus>(LoginStatus.INIT)

}