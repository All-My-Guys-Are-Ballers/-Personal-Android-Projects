package com.android.chatmeup.ui.screens.registeruserscreen

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.ui.screens.components.CmuDarkButton
import com.android.chatmeup.ui.screens.components.CmuInputTextField
import com.android.chatmeup.ui.screens.components.Logo
import com.android.chatmeup.ui.theme.cmuBlue
import com.android.chatmeup.util.isEmailValid
import com.android.chatmeup.util.isStrongPassword
import com.google.accompanist.insets.navigationBarsWithImePadding

@Composable
fun RegisterUserScreen(
    context: Context,
    activity: Activity?,
    onBackClick: () -> Unit,
    registerUserScreenViewModel: RegisterUserScreenViewModel = hiltViewModel()
){
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
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsWithImePadding()
                .padding(it)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            Logo()
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
            Spacer(modifier = Modifier.height(10.dp))
            CmuInputTextField(
                modifier = Modifier,
                label = "Email",
                placeholder = "Enter your email address",
                text = email,
                imeAction = ImeAction.Next,
                onValueChanged = { value ->
                    email.value = value
                    isEmailValid.value = email.value.text.isEmailValid()
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
                    password.value = value
                    isStrongPassword.value = isStrongPassword(password.value.text)
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
                    confirmPassword.value = value
                    isStrongPassword.value = isStrongPassword(password.value.text)
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
                    } else if (!isStrongPassword.value) {
                        CmuToast.createFancyToast(
                            context = context,
                            activity = activity,
                            title = "Sign Up",
                            message = "Password too weak. Make sure to use special characters and numbers",
                            style = CmuToastStyle.ERROR,
                            duration = CmuToastDuration.LONG
                        )
                    } else if (password.value != confirmPassword.value) {
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
                            email = email.value.text,
                            password = password.value.text
                        )
                    }
                }
            )

            Text(
                text = "or",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            )

            CmuDarkButton(
                label = "Sign Up with Google",
                isLoading = false,
                onClick = {
                    //TODO
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