package com.android.chatmeup.ui.screens.registeruserscreen

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread
import androidx.lifecycle.viewModelScope
import com.android.chatmeup.data.datastore.CmuDataStoreRepository
import com.android.chatmeup.data.db.repository.AuthRepository
import com.android.chatmeup.ui.DefaultViewModel
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.data.Result
import com.android.chatmeup.data.db.entity.User
import com.android.chatmeup.data.db.repository.DatabaseRepository
import com.fredrikbogg.android_chat_app.data.model.CreateUser
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RegisterUserStatus{
    INIT,
    LOADING,
    DONE,
    ERROR,
}

@HiltViewModel
class RegisterUserScreenViewModel @Inject constructor(cmuDataStoreRepository: CmuDataStoreRepository) : DefaultViewModel(){
    var cmuDataStoreRepository: CmuDataStoreRepository? = null
    private val tag: String = "RegisterUserScreenViewModel"
    val registerUserEventStatus = MutableStateFlow(RegisterUserStatus.INIT)
    private val authRepository = AuthRepository()
    private val dbRepository = DatabaseRepository()


    init {
        this.cmuDataStoreRepository = cmuDataStoreRepository
    }

    fun onEventTriggered(
        activity: Activity?,
        context: Context,
        event: RegisterUserEvents,
        email: String = "",
        password: String = "",
        displayName: String = "",
        onRegisterUser: () -> Unit = {},
    ){
        when(event){
            RegisterUserEvents.InitRegisterUserEvent -> {
                registerUserEventStatus.value = RegisterUserStatus.INIT
            }
            RegisterUserEvents.LoadingEvent -> {
                registerUserEventStatus.value = RegisterUserStatus.LOADING
                createUser(
                    displayName = displayName,
                    email = email,
                    password = password,
                    activity = activity,
                    context = context,
                    onRegisterUser = onRegisterUser
                )
            }
            RegisterUserEvents.DoneEvent -> {
                registerUserEventStatus.value = RegisterUserStatus.DONE
                Handler(Looper.getMainLooper()).postDelayed({
                    CmuToast.createFancyToast(
                        context,
                        activity,
                        "Register User",
                        "Account Created, Proceed to Login Page",
                        CmuToastStyle.SUCCESS,
                        CmuToastDuration.SHORT
                    )
                }, 200)
            }
            RegisterUserEvents.ErrorEvent -> {
                registerUserEventStatus.value = RegisterUserStatus.ERROR
                Handler(Looper.getMainLooper()).postDelayed({
                    CmuToast.createFancyToast(
                        context,
                        activity,
                        "Register User",
                        "Email already exists",
                        CmuToastStyle.ERROR,
                        CmuToastDuration.SHORT
                    )
                }, 200)
                onEventTriggered(
                    activity, context, RegisterUserEvents.InitRegisterUserEvent,
                )
            }
        }
    }

    private fun createUser(
        displayName: String,
        email: String,
        password: String,
        activity: Activity?,
        context: Context,
        onRegisterUser: () -> Unit
    ) {
        val createUser = CreateUser(displayName,email, password)

        authRepository.createUser(createUser) { result: Result<FirebaseUser> ->
            onResult(null, result)
            if (result is Result.Success) {
                dbRepository.updateNewUser(User().apply {
                    info.id = result.data?.uid.toString()
                    info.displayName = createUser.displayName
                    info.email = createUser.email
                })
                onEventTriggered(
                    activity = activity,
                    context = context,
                    event = RegisterUserEvents.DoneEvent,
                    onRegisterUser = onRegisterUser,
                )
            }
            else if (result is Result.Error) {
                onEventTriggered(
                    activity = activity,
                    context = context,
                    event = RegisterUserEvents.ErrorEvent,
                    onRegisterUser = onRegisterUser,
                )
            }
            else {
//                Timber.tag(tag).d("Still Loading")
            }
        }
    }

    @WorkerThread
    private fun saveUserId(value: String?) = viewModelScope.launch(Dispatchers.IO) {
        if (value != null) {
            cmuDataStoreRepository?.saveUserId(value)
        }
    }

    sealed class RegisterUserEvents(){
        object InitRegisterUserEvent: RegisterUserEvents()
        object LoadingEvent: RegisterUserEvents()
        object ErrorEvent: RegisterUserEvents()
        object DoneEvent: RegisterUserEvents()
    }
}
