package com.example.vehiclenumberidenticationapp.ui.screens

import android.content.ContentValues.TAG
import android.util.Log
import android.util.Patterns
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vehiclenumberidenticationapp.Destination
import com.example.vehiclenumberidenticationapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun LoginPage(modifier: Modifier = Modifier, navController: NavController, isAdmin: Boolean = false, auth:FirebaseAuth){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val isEmailValid by derivedStateOf {
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val isPasswordValid by derivedStateOf {
        password.length > 7
    }
    var isWrongUsernameOrPassword by remember {
        mutableStateOf(true )
    }
    val focusManager = LocalFocusManager.current
    Scaffold(topBar = { TopAppBar(navController = navController, isAdmin = isAdmin)}){
        Card(
            modifier = modifier
                .fillMaxSize()
                .wrapContentSize()
                .padding(16.dp)
                .wrapContentSize(),
            elevation = 16.dp
        ) {
            Column(modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AnimatedVisibility(visible = !isWrongUsernameOrPassword,
                    enter = fadeIn(),
                    exit = fadeOut()
                ){
                    Text(text = stringResource(id = R.string.wrong_credentials),
                        color = Color.Red,
                        style = MaterialTheme.typography.body1
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = {
                        Text(text = stringResource(id = R.string.email))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Next)
                    }),
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Person, contentDescription = "Avatar")
                    }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(text = stringResource(id = R.string.password))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Lock, contentDescription = "Lock")
                    },
                    trailingIcon = {
                        if (showPassword) {
                            IconButton(onClick = { showPassword = !showPassword }){
                                Icon(
                                    imageVector = Icons.Filled.Visibility,
                                    contentDescription = "Password Visibility On"
                                )
                            }
                        } else {
                            IconButton(onClick = { showPassword = !showPassword }){
                                Icon(
                                    imageVector = Icons.Filled.VisibilityOff,
                                    contentDescription = "Password Visibility Off"
                                )
                            }
                        }
                    },
                    visualTransformation = if (!showPassword) PasswordVisualTransformation() else VisualTransformation.None
                )
                Button(onClick = {
                                 auth.signInWithEmailAndPassword(email, password)
                                     .addOnCompleteListener {
                                         if(it.isSuccessful){
                                             navController.navigate(Destination.PoliceUserPage.createRoute(email))
                                         }
                                         else{
                                             isWrongUsernameOrPassword = false
                                         }
                                     }
                },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEmailValid && isPasswordValid
                ) {
                    Text(text = stringResource(id = R.string.login))
                }

                Button(onClick = {navController.navigate(Destination.RegisterUserScreen.route) }) {
                    Text(text = stringResource(id = R.string.create_account),
//                        style = MaterialTheme.typography.h4
                    )
                }
            }
        }
    }
}

@Composable
fun TopAppBar(navController: NavController, isAdmin: Boolean){
    val userStr = if (isAdmin) " an Admin" else " a Police Officer"
    Row(modifier = Modifier.wrapContentSize(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { navController.navigate(Destination.LoginChoicePage.route) }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go Back")
        }
        Text(
            text = stringResource(id = R.string.login_top_bar) + userStr,
            style = MaterialTheme.typography.h2
            )
    }
}

//fun F