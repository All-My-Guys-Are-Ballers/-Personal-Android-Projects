package com.android.chatmeup.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.android.chatmeup.CmuApplication
import com.android.chatmeup.CmuNavHost
import com.android.chatmeup.ui.screens.homescreen.navigation.HomeDestination
import com.android.chatmeup.ui.screens.loginscreen.LoginDestination
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CmuApp(
    chatMeUpApp: CmuApplication,
) {
    val appState: CmuAppState = rememberChatMeUpAppState(
        context = LocalContext.current,
        navController = rememberNavController(),
        activity = (LocalContext.current.applicationContext as CmuApplication).getCurrentActivity()
    )

    CmuNavHost(
        context = appState.appStateContext,
        activity = appState.appStateActivity,
        navController = appState.navController,
        onBackClick = appState::navigateBack,
        onNavigateToDestination = appState::navigate,
        startDestination = if(Firebase.auth.currentUser == null) LoginDestination.route else HomeDestination.route
    )
}