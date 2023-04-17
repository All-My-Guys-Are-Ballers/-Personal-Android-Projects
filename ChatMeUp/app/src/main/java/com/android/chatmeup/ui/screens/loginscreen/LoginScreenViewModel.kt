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
import com.android.chatmeup.datastore.CmuDataStoreRepository
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
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
}

@HiltViewModel
class LoginScreenViewModel @Inject constructor(cmuDataStoreRepository: CmuDataStoreRepository) : ViewModel(){
    var cmuDataStoreRepository: CmuDataStoreRepository? = null
    val tag: String = "LoginScreenViewModel"
    val loginEventStatus = MutableStateFlow(LoginStatus.INIT)
    private val auth = Firebase.auth

    init {
        this.cmuDataStoreRepository = cmuDataStoreRepository
    }


    fun onEventTriggered(
        activity: Activity?,
        context: Context,
        event: LoginEvents,
        email: String = "",
        password: String = "",
    ){
        when(event){
            LoginEvents.InitLoginEvent -> {
                loginEventStatus.value = LoginStatus.INIT
            }
            LoginEvents.LoadingEvent -> {
                loginEventStatus.value = LoginStatus.LOADING
                if (activity != null) {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Timber.tag(tag).d("signInUserWithEmail:success")
                            onEventTriggered(activity, context, LoginEvents.DoneEvent)
                        } else {
                            // If sign in fails, display a message to the user.
                            Timber.tag(tag).w(task.exception, "signInUserWithEmail:failure")
                            onEventTriggered(activity, context, LoginEvents.InitLoginEvent)
                            Handler(Looper.getMainLooper()).postDelayed({
                                CmuToast.createFancyToast(
                                    context,
                                    activity,
                                    "Login",
                                    "Unable to login. Incorrect Email or password",
                                    CmuToastStyle.ERROR,
                                    CmuToastDuration.SHORT
                                )
                            }, 200)
                        }
                    }
                }
            }
            LoginEvents.DoneEvent -> {
                loginEventStatus.value = LoginStatus.DONE
                //navigate to
            }
        }
    }

    @WorkerThread
    private fun saveLoginCredentials(value: String) = viewModelScope.launch(Dispatchers.IO) {
        cmuDataStoreRepository?.saveLoginCredentials(value)
//        _terminalPrintModeState.value = value
    }

    @WorkerThread
    fun getLoginCredentials() = viewModelScope.launch (
        Dispatchers.IO) {
        cmuDataStoreRepository?.getLoginCredentials()?.collect { state ->
            withContext(Dispatchers.IO) {
//                _terminalPrintModeState.value = state
            }
        }
    }


    sealed class LoginEvents(){
        object InitLoginEvent: LoginEvents()
        object LoadingEvent: LoginEvents()
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

