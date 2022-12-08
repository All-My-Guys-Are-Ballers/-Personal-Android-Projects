package com.example.vehiclenumberidenticationapp.ui.screens

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vehiclenumberidenticationapp.R

@Preview
@Composable
fun LoginPage(modifier: Modifier = Modifier, isAdmin: Boolean = false){
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Scaffold(topBar = { TopAppBar(isAdmin = isAdmin)}){
        Box(
            modifier = modifier
                .fillMaxSize()
                .wrapContentSize()
        ) {
            Column() {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(text = stringResource(id = R.string.username))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Next)
                    }),
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Person, contentDescription = "Avatar")
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(text = stringResource(id = R.string.password))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
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
                TextButton(onClick = { /*TODO*/ }) {
                    Text(text = stringResource(id = R.string.login))
                }
            }
        }
    }
}

@Composable
fun LoginType(){

}

@Composable
fun TopAppBar(isAdmin: Boolean){
    var userStr = if (isAdmin) " an Admin" else " a Police Officer"
    Row(modifier = Modifier.wrapContentSize(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go Back")
        }
        Text(
            text = stringResource(id = R.string.login_top_bar) + userStr,
            style = MaterialTheme.typography.h2
            )
    }
}

//fun F