package com.android.chatmeup.ui.screens.loginscreen

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

    var email  by rememberSaveable {
        mutableStateOf("")
    }

    var password  by rememberSaveable {
        mutableStateOf("")
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
            .verticalScroll(rememberScrollState())
            .navigationBarsWithImePadding()
            .padding(it)
            .fillMaxSize()) {
            Spacer(modifier = Modifier.height(30.dp))
            Logo()
            Spacer(modifier = Modifier.height(50.dp))
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Login to your account. Share long lasting memories",
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
                onValueChanged = {value ->
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
                imeAction = ImeAction.Done,
                onValueChanged = {value ->
                    password = value
                    isStrongPassword.value = isStrongPassword(password)
                },
                trailingIcon = {
                    Icon(imageVector = if(isPasswordVisible.value) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "Password visibility",
                        modifier = Modifier.clickable {
                            isPasswordVisible.value = !isPasswordVisible.value
                        }
                    )
            },
                visualTransformation = if(isPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            )
            
            Spacer(modifier = Modifier.height(30.dp))

            //TODO Forgot Password and Remember me

            CmuDarkButton(
                label = "Login",
                isLoading = loginViewState == LoginStatus.LOADING,
                onClick = {
                    if (isEmailValid.value) loginScreenViewModel.onEventTriggered(
                        activity,
                        context,
                        LoginScreenViewModel.LoginEvents.LoadingEvent,
                        email = email,
                        password = password,
                        onLoggedIn,
                    )
                    else {CmuToast.createFancyToast(
                        context = context,
                        activity = activity,
                        title = "Login",
                        message = "Invalid Email",
                        style = CmuToastStyle.ERROR,
                        duration = CmuToastDuration.SHORT
                    )}
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
                label = "Sign in with Google",
                isLoading = false, //TODO
                onClick = {
                    CmuToast.createFancyToast(
                        context,
                        activity,
                        "N/A",
                        "This feature has not been implemented yet",
                        CmuToastStyle.INFO,
                        CmuToastDuration.SHORT
                    )
                }
            )
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "Don't have an account? ",
//                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                )
                Text(
                    text = "Register" ,
                    style = MaterialTheme.typography.labelLarge,
                    color = cmuBlue,
                    modifier = Modifier.clickable {
                        onClickRegister()
                    },
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}