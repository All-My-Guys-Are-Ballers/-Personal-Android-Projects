package com.android.chatmeup.ui.screens.registeruserscreen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.ui.screens.components.CmuDarkButton
import com.android.chatmeup.ui.screens.components.CmuInputTextField
import com.android.chatmeup.ui.theme.cmuBlue
import com.android.chatmeup.util.createTempImageFile
import com.android.chatmeup.util.isEmailValid
import com.android.chatmeup.util.isStrongPassword
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RegisterUserScreen(
    context: Context,
    activity: Activity?,
    onBackClick: () -> Unit,
    registerUserScreenViewModel: RegisterUserScreenViewModel = hiltViewModel()
){
    val registerUserViewState by registerUserScreenViewModel.registerUserEventStatus.collectAsState()

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val storagePermissionState = rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)

    var photoURI: Uri? = null

    var imageUploaded by remember {
        mutableStateOf(false)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            // Retrieve the captured image URI from the camera
            photoURI?.let {
                registerUserScreenViewModel.onProfilePictureEventTriggered(
                    event = RegisterUserScreenViewModel.ProfilePictureEvents.LoadingEvent,
                    imageUri = it
                )
            }
            imageUploaded = true
        }
    }

    var email: String by remember {
        mutableStateOf("")
    }

    var displayName: String by remember {
        mutableStateOf("")
    }

    var password: String by remember {
        mutableStateOf("")
    }

    var confirmPassword: String by remember {
        mutableStateOf("")
    }

    val isEmailValid: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }

    val isDisplayNameValid: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }

    val isPasswordVisible: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }

    val isStrongPassword: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { it ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            CameraPermission(context = context,
//                permissionState = cameraPermissionState,
//                onImageSelected = {imageUri ->
//                    registerUserScreenViewModel.onProfilePictureEventTriggered(
//                        event = RegisterUserScreenViewModel.ProfilePictureEvents.LoadingEvent,
//                        imageUri = imageUri
//                    )
//                }
//            )
            Spacer(modifier = Modifier.height(50.dp))
            Text(
                text = "Create an account",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Connect with your friends by creating an account",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(15.dp))
            Card(
                modifier = Modifier.size(90.dp),
                shape = CircleShape,
                onClick = {
                    if(cameraPermissionState.hasPermission){
                        imageUploaded = false
                        if(photoURI != null){
                            context.contentResolver.delete(photoURI!!, null)
                        }
                        photoURI = FileProvider.getUriForFile(
                            context,
                            context.applicationContext.packageName + ".provider",
                            createTempImageFile(context)
                        )
                        cameraLauncher.launch(photoURI)
                    }
                    else{
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
            ) {
                if(!imageUploaded){
                    Icon(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        imageVector = Icons.Default.Person,
                        contentDescription = "Upload profile picture"
                    )
                }
                else{
                    Image(
                        painter = rememberAsyncImagePainter(photoURI),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            CmuInputTextField(
                modifier = Modifier,
                label = "Display Name",
                placeholder = "Enter your Display Name",
                text = displayName,
                imeAction = ImeAction.Next,
                onValueChanged = { value ->
                    displayName = value
                },
            )
            CmuInputTextField(
                modifier = Modifier,
                label = "Email",
                placeholder = "Enter your email address",
                text = email,
                imeAction = ImeAction.Next,
                onValueChanged = { value ->
                    email = value
                    isEmailValid.value = email.isEmailValid()
                },
            )
            CmuInputTextField(
                modifier = Modifier,
                label = "Password",
                placeholder = "Enter your password",
                keyboardType = KeyboardType.Password,
                text = password,
                imeAction = ImeAction.Next,
                onValueChanged = { value ->
                    password = value
                    isStrongPassword.value = isStrongPassword(password)
                },
                trailingIcon = {
                    Icon(imageVector = if (isPasswordVisible.value) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "Password visibility",
                        modifier = Modifier.clickable {
                            isPasswordVisible.value = !isPasswordVisible.value
                        }
                    )
                },
                visualTransformation = if (isPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            )
            CmuInputTextField(
                modifier = Modifier,
                label = "Confirm Password",
                placeholder = "Confirm",
                keyboardType = KeyboardType.Password,
                text = confirmPassword,
                imeAction = ImeAction.Done,
                onValueChanged = { value ->
                    confirmPassword = value
                    isStrongPassword.value = isStrongPassword(password)
                },
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(modifier = Modifier.height(30.dp))

            Column() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "By continuing you agree to our ",
//                    textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                    )
                    Text(
                        text = "Terms of Service and",
                        style = MaterialTheme.typography.labelLarge,
                        color = cmuBlue,
                        modifier = Modifier.clickable {
                            //TODO
                        }
                    )
                }
                Text(
                    text = "Privacy policy",
                    style = MaterialTheme.typography.labelLarge,
                    color = cmuBlue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            //TODO
                        }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            CmuDarkButton(
                label = "Sign Up",
                isLoading = registerUserViewState == RegisterUserStatus.LOADING,
                onClick = {
                    if (!isEmailValid.value) {
                        CmuToast.createFancyToast(
                            context = context,
                            activity = activity,
                            title = "Sign Up",
                            message = "Invalid Email",
                            style = CmuToastStyle.ERROR,
                            duration = CmuToastDuration.SHORT
                        )
                    }
//                    else if (!isStrongPassword.value) {
//                        CmuToast.createFancyToast(
//                            context = context,
//                            activity = activity,
//                            title = "Sign Up",
//                            message = "Password too weak. Make sure to use special characters and numbers",
//                            style = CmuToastStyle.ERROR,
//                            duration = CmuToastDuration.LONG
//                        )
//                    }
                else if (password != confirmPassword) {
                        CmuToast.createFancyToast(
                            context = context,
                            activity = activity,
                            title = "Sign Up",
                            message = "Passwords don't match",
                            style = CmuToastStyle.ERROR,
                            duration = CmuToastDuration.LONG
                        )
                    } else {
                        registerUserScreenViewModel.onEventTriggered(
                            activity,
                            context,
                            RegisterUserScreenViewModel.RegisterUserEvents.LoadingEvent,
                            email = email,
                            password = password,
                            imageUri = photoURI,
                            displayName = displayName,
                            onRegisterUser = onBackClick
                        )
                    }
                }
            )

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "Already have an account? ",
//                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                )
                Text(
                    text = "Login" ,
                    style = MaterialTheme.typography.labelLarge,
                    color = cmuBlue,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        onBackClick()
                    }
                )
            }

//            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}