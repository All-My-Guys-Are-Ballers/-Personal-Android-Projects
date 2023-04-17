package com.android.chatmeup.ui.screens.registeruserscreen

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
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun RegisterUserScreen(
    context: Context,
    activity: Activity?,
    onBackClick: () -> Unit,
){
    val registerUserScreenViewModel = viewModel<RegisterUserScreenViewModel>()

    val registerUserViewState by registerUserScreenViewModel.registerUserEventStatus.collectAsState()

    val email: MutableState<TextFieldValue> = remember {
        mutableStateOf(TextFieldValue(""))
    }

    val password: MutableState<TextFieldValue> = remember {
        mutableStateOf(TextFieldValue(""))
    }

    val confirmPassword: MutableState<TextFieldValue> = remember {
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
                color = MaterialTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(50.dp))
            Text(
                text = "Create an account",
                style = MaterialTheme.typography.h4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Connect with your friends by creating an account",
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
                imeAction = ImeAction.Next,
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
            CmuInputTextField(
                label = "Confirm Password",
                modifier = Modifier,
                placeholder = "Confirm",
                keyboardType = KeyboardType.Password,
                text = confirmPassword,
                imeAction = ImeAction.Done,
                visualTransformation = PasswordVisualTransformation(),
                onValueChanged = {value ->
                    confirmPassword.value = value
                    isStrongPassword.value = isStrongPassword(password.value.text)
                },
            )
            Spacer(modifier = Modifier.height(30.dp))

            Column(){
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
                        style = MaterialTheme.typography.button,
                        modifier = Modifier
                    )
                    Text(
                        text = "Terms of Service and",
                        style = MaterialTheme.typography.button,
                        color = cmuBlue,
                        modifier = Modifier.clickable {
                            //TODO
                        }
                    )
                }
                Text(
                    text = "Privacy policy",
                    style = MaterialTheme.typography.button,
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

            Button(modifier = Modifier
                .padding(start = 80.dp, end = 80.dp)
                .height(50.dp)
                .fillMaxWidth(),
                onClick = {

                    if(!isEmailValid.value){
                        CmuToast.createFancyToast(
                        context = context,
                        activity = activity,
                        title = "Sign Up",
                        message = "Invalid Email",
                        style = CmuToastStyle.ERROR,
                        duration = CmuToastDuration.SHORT
                    )}
                    else if(!isStrongPassword.value){
                        CmuToast.createFancyToast(
                            context = context,
                            activity = activity,
                            title = "Sign Up",
                            message = "Password too weak. Make sure to use special characters and numbers",
                            style = CmuToastStyle.ERROR,
                            duration = CmuToastDuration.LONG
                        )
                    }
                    else if(password.value != confirmPassword.value){
                        CmuToast.createFancyToast(
                            context = context,
                            activity = activity,
                            title = "Sign Up",
                            message = "Passwords don't match",
                            style = CmuToastStyle.ERROR,
                            duration = CmuToastDuration.LONG
                        )
                    }
                    else {
                        registerUserScreenViewModel.onEventTriggered(
                            activity,
                            context,
                            RegisterUserScreenViewModel.RegisterUserEvents.LoadingEvent,
                            email = email.value.text,
                            password = password.value.text
                        )
                    }
                },elevation =  ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 5.dp,
                    disabledElevation = 0.dp
                ), colors = ButtonDefaults.buttonColors(backgroundColor = cmuBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (registerUserViewState == RegisterUserStatus.LOADING){
                    CircularProgressIndicator(
                        color = cmuWhite,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        strokeWidth = 2.dp
                    )
                }
                else{
                    Text(
                        text = "Sign Up",
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
                    text = "Already have an account? ",
//                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.button,
                    modifier = Modifier
                )
                Text(
                    text = "Login" ,
                    style = MaterialTheme.typography.button,
                    color = cmuBlue,
                    modifier = Modifier.clickable {
                        onBackClick()
                    }
                )
            }
        }
    }
}