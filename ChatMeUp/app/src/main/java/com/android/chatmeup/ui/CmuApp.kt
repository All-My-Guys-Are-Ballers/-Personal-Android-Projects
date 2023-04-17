package com.android.chatmeup.ui

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.android.chatmeup.CmuApplication
import com.android.chatmeup.datastore.CmuDataStoreRepository
import com.android.chatmeup.navigation.CmuNavHost
import com.android.chatmeup.ui.screens.loginscreen.LoginDestination

@Composable
fun CmuApp(
    context: Context,
    activity: Activity,
    navController: NavHostController,
    chatMeUpApp: CmuApplication
) {
    val appState: CmuAppState = rememberChatMeUpAppState(
        context = context,
        navController = navController,
        activity = activity
    )
//    val windowSize: NxWindowSize = rememberWindowSize()

    CmuNavHost(
        context = appState.appStateContext,
        activity = appState.appStateActivity,
//        windowSize = windowSize,
        navController = appState.navController,
        onBackClick = appState::navigateBack,
        onNavigateToDestination = appState::navigate,
        startDestination = LoginDestination.route
    )
}