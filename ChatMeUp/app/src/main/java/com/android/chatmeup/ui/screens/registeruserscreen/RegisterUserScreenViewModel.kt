package com.android.chatmeup.ui.screens.registeruserscreen

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.android.chatmeup.data.Result
import com.android.chatmeup.data.datastore.CmuDataStoreRepository
import com.android.chatmeup.data.db.entity.User
import com.android.chatmeup.data.db.repository.AuthRepository
import com.android.chatmeup.data.db.repository.DatabaseRepository
import com.android.chatmeup.data.db.repository.StorageRepository
import com.android.chatmeup.ui.DefaultViewModel
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.util.SharedPreferencesUtil
import com.android.chatmeup.util.convertFileToByteArray
import com.fredrikbogg.android_chat_app.data.model.CreateUser
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

enum class RegisterUserStatus{
    INIT,
    LOADING,
    DONE,
    ERROR,
}

enum class ProfilePictureStatus{
    INIT,
    LOADING,
    DONE,
    ERROR,
}

@HiltViewModel
class RegisterUserScreenViewModel @Inject constructor(cmuDataStoreRepository: CmuDataStoreRepository) : DefaultViewModel(){
    var cmuDataStoreRepository: CmuDataStoreRepository? = null
    private val tag: String = "RegisterUserScreenViewModel"

    private val _registerUserEventStatus = MutableStateFlow(RegisterUserStatus.INIT)
    val registerUserEventStatus = _registerUserEventStatus.asStateFlow()

    private val _profilePictureEventStatus = MutableStateFlow(ProfilePictureStatus.INIT)
    val profilePictureStatus = _profilePictureEventStatus.asStateFlow()

    private val authRepository = AuthRepository()
    private val dbRepository = DatabaseRepository()
    private val storageRepository = StorageRepository()


    init {
        this.cmuDataStoreRepository = cmuDataStoreRepository
    }

    fun onEventTriggered(
        activity: Activity?,
        context: Context,
        event: RegisterUserEvents,
        email: String = "",
        password: String = "",
        imageUri: Uri? = null,
        displayName: String = "",
        myUserId: String? = "",
        errorMessage: String = "",
        onRegisterUser: () -> Unit = {},
    ){
        when(event){
            RegisterUserEvents.InitRegisterUserEvent -> {
                _registerUserEventStatus.value = RegisterUserStatus.INIT
            }
            RegisterUserEvents.LoadingEvent -> {
                _registerUserEventStatus.value = RegisterUserStatus.LOADING
                createUser(
                    displayName = displayName,
                    email = email,
                    password = password,
                    activity = activity,
                    context = context,
                    onRegisterUser = onRegisterUser,
                    imageUri = imageUri
                )
            }
            RegisterUserEvents.DoneEvent -> {
                _registerUserEventStatus.value = RegisterUserStatus.DONE
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
                try{ saveUserId(context, myUserId!!) }
                catch(e: Exception){
                    Timber.tag(tag).d("Error: $e")
                }
            }
            RegisterUserEvents.ErrorEvent -> {
                _registerUserEventStatus.value = RegisterUserStatus.ERROR
                Handler(Looper.getMainLooper()).postDelayed({
                    CmuToast.createFancyToast(
                        context,
                        activity,
                        "Register User",
                        errorMessage,
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

    fun onProfilePictureEventTriggered(
        event: ProfilePictureEvents,
        imageUri: Uri
    ){
        when(event){
            ProfilePictureEvents.Init -> {
                _profilePictureEventStatus.value = ProfilePictureStatus.INIT
            }

            ProfilePictureEvents.DoneEvent -> {
                _profilePictureEventStatus.value = ProfilePictureStatus.DONE
            }
            ProfilePictureEvents.ErrorEvent -> {
                _profilePictureEventStatus.value = ProfilePictureStatus.ERROR
            }
            ProfilePictureEvents.LoadingEvent -> {
                _profilePictureEventStatus.value = ProfilePictureStatus.LOADING
                Timber.tag(tag).d("ImageURI: $imageUri")
            }
        }
    }

    fun changeUserImage(userID: String, byteArray: ByteArray) {
        storageRepository.updateUserProfileImage(userID, byteArray) { result: Result<Uri> ->
            onResult(null, result)
            if (result is Result.Success) {
                dbRepository.updateUserProfileImageUrl(userID, result.data.toString())
            }
        }
    }

    private fun createUser(
        displayName: String,
        email: String,
        password: String,
        imageUri: Uri?,
        activity: Activity?,
        context: Context,
        onRegisterUser: () -> Unit
    ) {
        val createUser = CreateUser(displayName,email, password)

        authRepository.createUser(createUser) { result: Result<FirebaseUser> ->
            onResult(null, result)
            if (result is Result.Success) {
                result.data?.uid?.let {uid ->
                    if(imageUri != null){
                        storageRepository.updateUserProfileImage(
                            uid,
                            byteArray = convertFileToByteArray(context, imageUri)
                        ) { uploadResult: Result<Uri> ->
                            onResult(null, uploadResult)
                            if (uploadResult is Result.Success) {
                                dbRepository.updateNewUser(User().apply {
                                    info.id = uid
                                    info.displayName = createUser.displayName
                                    info.email = createUser.email
                                    info.profileImageUrl = uploadResult.data.toString()
                                })
                            } else if (uploadResult is Result.Error) {
                                uploadResult.msg?.let {
                                    onEventTriggered(
                                        activity = activity,
                                        context = context,
                                        event = RegisterUserEvents.ErrorEvent,
                                        onRegisterUser = onRegisterUser,
                                        errorMessage = it
                                    )
                                }
                            }
                        }
                    }
                    else{
                        dbRepository.updateNewUser(User().apply {
                            info.id = uid
                            info.displayName = createUser.displayName
                            info.email = createUser.email
                        })
                    }
                }

//                saveUserId(result.data?.uid.toString())
                onEventTriggered(
                    activity = activity,
                    context = context,
                    event = RegisterUserEvents.DoneEvent,
                    onRegisterUser = onRegisterUser,
                    myUserId = result.data?.uid
                )
            }
            else if (result is Result.Error) {
                result.msg?.let {
                    onEventTriggered(
                        activity = activity,
                        context = context,
                        event = RegisterUserEvents.ErrorEvent,
                        onRegisterUser = onRegisterUser,
                        errorMessage = it
                    )
                }
            }
            else {
//                Timber.tag(tag).d("Still Loading")
            }
        }
    }

    private fun saveUserId(context: Context, value: String) {
        SharedPreferencesUtil.saveUserID(context, value)
    }

    sealed class RegisterUserEvents(){
        object InitRegisterUserEvent: RegisterUserEvents()
        object LoadingEvent: RegisterUserEvents()
        object ErrorEvent: RegisterUserEvents()
        object DoneEvent: RegisterUserEvents()
    }

    sealed class ProfilePictureEvents(){
        object Init: ProfilePictureEvents()
        object LoadingEvent: ProfilePictureEvents()
        object ErrorEvent: ProfilePictureEvents()
        object DoneEvent: ProfilePictureEvents()
    }
}
