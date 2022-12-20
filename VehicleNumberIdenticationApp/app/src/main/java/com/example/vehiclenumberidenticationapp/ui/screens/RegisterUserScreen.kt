package com.example.vehiclenumberidenticationapp.ui.screens

import android.content.ContentValues.TAG
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vehiclenumberidenticationapp.Destination
import com.example.vehiclenumberidenticationapp.R
import com.example.vehiclenumberidenticationapp.models.PoliceUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.delay

enum class Status{
    NOTCLICKED,
    LOADING,
    SUCCESSFUL,
    FAILED,
}

@Composable
fun RegisterUserScreen(navController: NavController, auth: FirebaseAuth){
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var password by remember { mutableStateOf("") }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val isEmailValid by derivedStateOf {
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val isPasswordValid by derivedStateOf {
        password.length > 7 && password == confirmPassword
    }
    var isInvalidPassword by remember {
        mutableStateOf(false )
    }
    var isInvalidEmail by remember {
        mutableStateOf(false )
    }
    var status by remember {
        mutableStateOf(Status.NOTCLICKED)
    }

    val policeUser by lazy {
        PoliceUser(
            email = email,
            firstName = firstName,
            lastName = lastName,
            password = password
        )
    }
    val ctx = LocalContext.current

    Scaffold(topBar = { RegisterUserTopAppBar(navController) }) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
                .padding(16.dp)
                .wrapContentSize(),
            elevation = 16.dp
        ){
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AnimatedVisibility(
                    visible = isInvalidPassword,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = stringResource(id = R.string.invalid_credentials),
                        color = Color.Red,
                        style = MaterialTheme.typography.body1
                    )
                }
                AnimatedVisibility(
                    visible = isInvalidEmail,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = stringResource(id = R.string.invalid_email),
                        color = Color.Red,
                        style = MaterialTheme.typography.body1
                    )
                }

                NamesTextField(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    firstName = firstName,
                    lastName = lastName,
                    onFirstNameChange = { firstName = it },
                    onLastNameChange = { lastName = it },
                    focusManager = focusManager
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    label = {
                        Text(text = stringResource(id = R.string.email))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Next)
                    }),
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Person, contentDescription = "Avatar")
                    }
                )

                PasswordTextField(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    password = password,
                    onValueChange = { password = it },
                    focusManager = focusManager
                )
                ConfirmPasswordTextField(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    confirmPassword = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    focusManager = focusManager
                )

//                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (!isEmailValid) isInvalidEmail = true
                        else if (!isPasswordValid) isInvalidPassword = true
                        else {
                            status = Status.LOADING
                            status = createUser(auth, policeUser)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(240.dp),
//                enabled = isEmailValid && isPasswordValid
                ) {
                    when (status) {
                        Status.NOTCLICKED -> Text(text = stringResource(id = R.string.register))
                        Status.LOADING -> {
                            Row() {
                                Text(
                                    text = stringResource(id = R.string.creating_account),
                                    style = MaterialTheme.typography.body1
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                LoadingAnimation3(circleSize = 12.dp)
                            }
                        }
                        Status.SUCCESSFUL -> {
                            navController.navigate(Destination.LoginPage.route)
                            Toast.makeText(
                                ctx, "Account Created for ${policeUser.fullName}",
                                Toast.LENGTH_SHORT
                            ).show()
                            status = Status.NOTCLICKED
                        }
                        else -> {
                            Toast.makeText(
                                ctx, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                            status = Status.NOTCLICKED
                        }
                    }
                }
            }
        }
    }
    }

@Composable
fun NamesTextField(modifier: Modifier = Modifier, firstName: String, lastName: String, onFirstNameChange:(String) -> Unit, onLastNameChange: (String) -> Unit, focusManager:FocusManager){
    Column(modifier = modifier){
        OutlinedTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            modifier = Modifier.align(Alignment.CenterHorizontally),
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
            modifier = Modifier.align(Alignment.CenterHorizontally),
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

@Composable
fun PasswordTextField(modifier: Modifier = Modifier, password: String, onValueChange: (String) -> Unit, focusManager: FocusManager){
    var showPassword by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = password,
        onValueChange = onValueChange,
        modifier = modifier,
        label = {
            Text(text = stringResource(id = R.string.password))
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Password
        ),
        keyboardActions = KeyboardActions(onNext = {
            focusManager.moveFocus(FocusDirection.Next)
        }),
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Lock, contentDescription = "Password Icon")
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
}

@Composable
fun ConfirmPasswordTextField(modifier: Modifier = Modifier, confirmPassword: String, onValueChange: (String) -> Unit, focusManager: FocusManager){
    var showPassword by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onValueChange,
        modifier = modifier,
        label = {
            Text(text = stringResource(id = R.string.confirm_password))
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Password
        ),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
        }),
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Lock, contentDescription = "Password Icon")
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
}


@Composable
fun RegisterUserTopAppBar(navController: NavController){
    Row() {
        IconButton(onClick = { navController.navigate(Destination.LoginPage.route) }){
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go back")
        }

        Text(
            text = stringResource(id = R.string.create_account),
            modifier = Modifier.align(Alignment.CenterVertically),
            style = MaterialTheme.typography.h2,
            textAlign = TextAlign.Center
        )

    }
}

fun createUser(auth: FirebaseAuth, policeUser: PoliceUser): Status {
    var isSuccessful = false
//    val baseContext = LocalContext.current
    auth.createUserWithEmailAndPassword(policeUser.email, policeUser.password, )
        .addOnCompleteListener() { task ->
            isSuccessful = task.isSuccessful
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "createUserWithEmail:success")
                //                updateUI(user)
                auth.currentUser?.updateProfile(userProfileChangeRequest {
                    displayName = policeUser.fullName
                }
                )
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
//                Toast.makeText(baseContext, "Authentication failed.",
//                    Toast.LENGTH_SHORT).show()
//                updateUI(null)
            }
        }
    return if (!isSuccessful) Status.SUCCESSFUL else Status.FAILED
}

@Composable
fun LoadingAnimation3(
    circleColor: Color = Color(0xFF35898F),
    circleSize: Dp = 36.dp,
    animationDelay: Int = 400,
    initialAlpha: Float = 0.3f
) {

    // 3 circles
    val circles = listOf(
        remember {
            Animatable(initialValue = initialAlpha)
        },
        remember {
            Animatable(initialValue = initialAlpha)
        },
        remember {
            Animatable(initialValue = initialAlpha)
        }
    )

    circles.forEachIndexed { index, animatable ->

        LaunchedEffect(Unit) {

            // Use coroutine delay to sync animations
            delay(timeMillis = (animationDelay / circles.size).toLong() * index)

            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDelay
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    // container for circles
    Row(
        modifier = Modifier
        //.border(width = 2.dp, color = Color.Magenta)
    ) {

        // adding each circle
        circles.forEachIndexed { index, animatable ->

            // gap between the circles
            if (index != 0) {
                Spacer(modifier = Modifier.width(width = 6.dp))
            }

            Box(
                modifier = Modifier
                    .size(size = circleSize)
                    .clip(shape = CircleShape)
                    .background(
                        color = circleColor
                            .copy(alpha = animatable.value)
                    )
            ) {
            }
        }
    }
}