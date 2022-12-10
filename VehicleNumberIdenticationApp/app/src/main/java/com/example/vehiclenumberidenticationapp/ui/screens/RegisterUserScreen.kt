package com.example.vehiclenumberidenticationapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vehiclenumberidenticationapp.Destination
import com.example.vehiclenumberidenticationapp.R

@Composable
fun RegisterUserScreen(navController: NavController){
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var password by remember { mutableStateOf("") }
    var showPassword
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }


    Scaffold() {
        Column() {
            NamesTextField(
                modifier = Modifier,
                firstName = firstName,
                lastName = lastName,
                onFirstNameChange = {firstName = it},
                onLastNameChange = {lastName = it},
                focusManager = focusManager
            )


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
                trailingIcon = {},
                visualTransformation = PasswordVisualTransformation()
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
        }

        }
    }

@Composable
fun NamesTextField(modifier: Modifier = Modifier, firstName: String, lastName: String, onFirstNameChange:(String) -> Unit, onLastNameChange: (String) -> Unit, focusManager:FocusManager){
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)){
        OutlinedTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            modifier = Modifier.weight(1f),
            label = {
                Text(text = stringResource(id = R.string.first_name))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Next)
            }),
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Person, contentDescription = "Avatar")
            }
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            modifier = Modifier.weight(1f),
            label = {
                Text(text = stringResource(id = R.string.last_name))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Next)
            }),
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Person, contentDescription = "Avatar")
            }
        )
    }
}