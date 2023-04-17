package com.android.chatmeup.ui.screens.loginscreen

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.chatmeup.datastore.CmuDataStoreRepository
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.ui.screens.components.CmuInputTextField
import com.android.chatmeup.ui.theme.cmuBlue
import com.android.chatmeup.ui.theme.cmuWhite
import com.android.chatmeup.util.isEmailValid
import com.android.chatmeup.util.isStrongPassword

@Composable
fun LoginScreen(
    context: Context,
    activity: Activity?,
    onClickRegister: () -> Unit,
    onLoggedIn: () -> Unit,
    loginScreenViewModel: LoginScreenViewModel = hiltViewModel(),
){
//    val loginScreenViewModel = viewModel<LoginScreenViewModel>()
//    val loginScreenViewModel: LoginScreenViewModel = loginScreenViewModelProvider(
//        factory = factory,
//        cmuDataStoreRepository = cmuDataStoreRepository
//    )

    val loginViewState by loginScreenViewModel.loginEventStatus.collectAsState()

    val email: MutableState<TextFieldValue>  = remember {
        mutableStateOf(TextFieldValue(""))
    }

    val password: MutableState<TextFieldValue>  = remember {
        mutableStateOf(TextFieldValue(""))
    }

    val isEmailValid: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }

    val isPasswordVisible: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }

    val isStrongPassword: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { it ->
        Column(modifier = Modifier
            .padding(it)
            .fillMaxSize()) {
            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "ChatMeUp",
                style = MaterialTheme.typography.h3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp),
                textAlign = TextAlign.Start,
                color = cmuBlue
            )
            Spacer(modifier = Modifier.height(50.dp))
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.h4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Login to your account. Share long lasting memories",
                style = MaterialTheme.typography.button,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(10.dp))
            CmuInputTextField(
                label = "Email",
                placeholder = "Enter your email address",
                modifier = Modifier,
                text = email,
                imeAction = ImeAction.Next,
                onValueChanged = {value ->
                    email.value = value
                    isEmailValid.value = email.value.text.isEmailValid()
                },
            )
            CmuInputTextField(
                label = "Password",
                modifier = Modifier,
                placeholder = "Enter your password",
                keyboardType = KeyboardType.Password,
                text = password,
                imeAction = ImeAction.Done,
                trailingIcon = {
                    Icon(imageVector = if(isPasswordVisible.value) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "Password visibility",
                        modifier = Modifier.clickable {
                            isPasswordVisible.value = !isPasswordVisible.value
                        }
                    )
            },
                visualTransformation = if(isPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                onValueChanged = {value ->
                    password.value = value
                    isStrongPassword.value = isStrongPassword(password.value.text)
                },
            )
            
            Spacer(modifier = Modifier.height(30.dp))

            //TODO Forgot Password and Remember me

            Button(modifier = Modifier
                .padding(start = 80.dp, end = 80.dp)
                .height(50.dp)
                .fillMaxWidth(),
                onClick = {
                    if (isEmailValid.value) loginScreenViewModel.onEventTriggered(
                        activity,
                        context,
                        LoginScreenViewModel.LoginEvents.LoadingEvent,
                        email = email.value.text,
                        password = password.value.text
                    )
                    else {CmuToast.createFancyToast(
                        context = context,
                        activity = activity,
                        title = "Login",
                        message = "Invalid Email",
                        style = CmuToastStyle.ERROR,
                        duration = CmuToastDuration.SHORT
                    )}
                },elevation =  ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 5.dp,
                    disabledElevation = 0.dp
                ), colors = ButtonDefaults.buttonColors(backgroundColor = cmuBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (loginViewState == LoginStatus.LOADING){
                    CircularProgressIndicator(
                        color = cmuWhite,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        strokeWidth = 2.dp
                    )
                }
                else{
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.button,
                        color = cmuWhite
                    )
                }
            }

//            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "or",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.button,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            )
//            Spacer(modifier = Modifier.height(5.dp))
            Button(modifier = Modifier
                .padding(bottom = 20.dp, start = 80.dp, end = 80.dp)
                .height(50.dp)
                .fillMaxWidth()
                ,
                onClick = {
                    //TODO onClickSignInWithGoogle()
                    //your onclick code here
                },elevation =  ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 5.dp,
                    disabledElevation = 0.dp
                ), colors = ButtonDefaults.buttonColors(backgroundColor = cmuWhite),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(width = 2.dp, color = cmuBlue)
            ) {
//                Icon(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = "Google logo")
                Text(text = "Sign in with Google",
                    style = MaterialTheme.typography.button,
                    color = cmuBlue
                )
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "Don't have an account? ",
//                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.button,
                    modifier = Modifier
                )
                Text(
                    text = "Register" ,
                    style = MaterialTheme.typography.button,
                    color = cmuBlue,
                    modifier = Modifier.clickable {
                        onClickRegister()
                    }
                )
            }
        }
    }
}