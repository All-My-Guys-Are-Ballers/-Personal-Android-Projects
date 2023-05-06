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
import com.android.chatmeup.data.datastore.CmuDataStoreRepository
import com.android.chatmeup.data.db.repository.AuthRepository
import com.android.chatmeup.ui.DefaultViewModel
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.data.Result
import com.android.chatmeup.util.SharedPreferencesUtil
import com.fredrikbogg.android_chat_app.data.model.Login
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

enum class LoginStatus{
    INIT,
    LOADING,
    DONE,
    ERROR,
}

@HiltViewModel
class LoginScreenViewModel @Inject constructor(cmuDataStoreRepository: CmuDataStoreRepository) : DefaultViewModel(){
    var cmuDataStoreRepository: CmuDataStoreRepository? = null
    val tag: String = "LoginScreenViewModel"
    val loginEventStatus = MutableStateFlow(LoginStatus.INIT)
    private val authRepository = AuthRepository()
    val loginCredentials = MutableStateFlow("")


    init {
        this.cmuDataStoreRepository = cmuDataStoreRepository
    }


    fun onEventTriggered(
        activity: Activity?,
        context: Context,
        event: LoginEvents,
        email: String = "",
        password: String = "",
        onLoggedIn: () -> Unit = {},
        myUserId: String? = "",
    ){
        when(event){
            LoginEvents.InitLoginEvent -> {
                loginEventStatus.value = LoginStatus.INIT
            }
            LoginEvents.LoadingEvent -> {
                loginEventStatus.value = LoginStatus.LOADING
                login(
                    email = email,
                    password = password,
                    context = context,
                    activity = activity,
                    onLoggedIn = onLoggedIn
                )
            }
            LoginEvents.ErrorEvent -> {
                loginEventStatus.value = LoginStatus.ERROR
                Handler(Looper.getMainLooper()).postDelayed({
                    CmuToast.createFancyToast(
                        context,
                        activity,
                        "Login Error",
                        "Invalid Username or Password",
                        CmuToastStyle.ERROR,
                        CmuToastDuration.SHORT
                    )
                }, 200)
                onEventTriggered(
                    activity, context, LoginEvents.InitLoginEvent
                )
            }
            LoginEvents.DoneEvent -> {
                loginEventStatus.value = LoginStatus.DONE
                try{ saveUserId(context, myUserId!!) }
                catch(e: Exception){
                    Timber.tag(tag).d("Error: $e")
                }
                onLoggedIn()
            }
        }
    }

    private fun saveUserId(context: Context, value: String) {
        SharedPreferencesUtil.saveUserID(context, value)
    }

    @WorkerThread
    private fun saveLoginCredentials(value: String) = viewModelScope.launch(Dispatchers.IO) {
        cmuDataStoreRepository?.saveLoginCredentials(value)
    }

    @WorkerThread
    fun getLoginCredentials() = viewModelScope.launch (
        Dispatchers.IO) {
        cmuDataStoreRepository?.getLoginCredentials()?.collect { state ->
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
        onLoggedIn: () -> Unit
    ) {
        val login = Login(email, password)

        authRepository.loginUser(login) { result: Result<FirebaseUser> ->
            onResult(null, result)
            if (result is Result.Success) {
//                saveUserId(result.data?.uid)
                onEventTriggered(
                    activity = activity,
                    context = context,
                    event = LoginEvents.DoneEvent,
                    onLoggedIn = onLoggedIn,
                    myUserId = result.data?.uid
                )
            }
            else if (result is Result.Error) {
                onEventTriggered(
                    activity = activity,
                    context = context,
                    event = LoginEvents.ErrorEvent,
                    onLoggedIn = onLoggedIn,
                )
            }
            else {
//                Timber.tag(tag).d("Still Loading")
            }
        }
    }



    sealed class LoginEvents(){
        object InitLoginEvent: LoginEvents()
        object LoadingEvent: LoginEvents()
        object ErrorEvent: LoginEvents()
        object DoneEvent: LoginEvents()
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

