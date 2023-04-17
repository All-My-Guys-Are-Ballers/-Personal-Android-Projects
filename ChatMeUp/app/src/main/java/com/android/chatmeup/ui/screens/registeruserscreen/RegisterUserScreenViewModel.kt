package com.android.chatmeup.ui.screens.registeruserscreen

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

enum class RegisterUserStatus{
    INIT,
    LOADING,
    DONE,
}

class RegisterUserScreenViewModel() : ViewModel(){
    val tag: String = "RegisterUserScreenViewModel"
    val registerUserEventStatus = MutableStateFlow(RegisterUserStatus.INIT)
    private val auth = Firebase.auth


    fun onEventTriggered(
        activity: Activity?,
        context: Context,
        event: RegisterUserEvents,
        email: String = "",
        password: String = "",
    ){
        when(event){
            RegisterUserEvents.InitRegisterUserEvent -> {
                registerUserEventStatus.value = RegisterUserStatus.INIT
            }
            RegisterUserEvents.LoadingEvent -> {
                registerUserEventStatus.value = RegisterUserStatus.LOADING
                if (activity != null) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Timber.tag(tag).d("signUpUserWithEmail:success")
                            onEventTriggered(activity, context, RegisterUserEvents.DoneEvent)
                            Handler(Looper.getMainLooper()).postDelayed({
                                Timber.tag(tag).w(task.exception, "signUpUserWithEmail:failure")
                                CmuToast.createFancyToast(
                                    context,
                                    activity,
                                    "User Registration",
                                    "User created",
                                    CmuToastStyle.SUCCESS,
                                    CmuToastDuration.SHORT
                                )
                            }, 200)
                        } else {
                            // If sign in fails, display a message to the user.
                            onEventTriggered(activity, context, RegisterUserEvents.InitRegisterUserEvent)
                            Handler(Looper.getMainLooper()).postDelayed({
                                Timber.tag(tag).w(task.exception, "signUpUserWithEmail:failure")
                                CmuToast.createFancyToast(
                                    context,
                                    activity,
                                    "User Registration",
                                    "User already exists",
                                    CmuToastStyle.ERROR,
                                    CmuToastDuration.SHORT
                                )
                            }, 200)
                        }
                    }
                }
            }
            RegisterUserEvents.DoneEvent -> {
                registerUserEventStatus.value = RegisterUserStatus.DONE
            }
        }
    }

    sealed class RegisterUserEvents(){
        object InitRegisterUserEvent: RegisterUserEvents()
        object LoadingEvent: RegisterUserEvents()
        object DoneEvent: RegisterUserEvents()
    }
}
