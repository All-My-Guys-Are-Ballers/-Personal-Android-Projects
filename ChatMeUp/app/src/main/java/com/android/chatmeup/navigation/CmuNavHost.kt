package com.android.chatmeup.navigation

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.android.chatmeup.MainActivity
import com.android.chatmeup.ui.screens.chat.navigation.ChatDestination
import com.android.chatmeup.ui.screens.chat.navigation.chatGraph
import com.android.chatmeup.ui.screens.homescreen.navigation.HomeDestination
import com.android.chatmeup.ui.screens.homescreen.navigation.homeGraph
import com.android.chatmeup.ui.screens.loginscreen.LoginDestination
import com.android.chatmeup.ui.screens.loginscreen.loginGraph
import com.android.chatmeup.ui.screens.registeruserscreen.RegisterUserDestination
import com.android.chatmeup.ui.screens.registeruserscreen.registerUserGraph
import com.android.chatmeup.ui.screens.settings.navigation.SettingsDestination
import com.android.chatmeup.ui.screens.settings.navigation.settingsGraph
import dagger.hilt.android.EntryPointAccessors

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CmuNavHost(
    context: Context,
    activity: Activity?,
    navController: NavHostController,
    onNavigateToDestination: (NavBackStackEntry, CmuNavigationDestination, String) -> Unit,
    startDestination: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
//    onBackPressedDispatcher: OnBackPressedDispatcher,
    myUserId: String,
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ){
        loginGraph(
            context = context,
            activity = activity,
            onClickRegister = {
                onNavigateToDestination(it, RegisterUserDestination, RegisterUserDestination.route)
            },
            onLoggedIn = {
                onNavigateToDestination(it, HomeDestination, HomeDestination.route)
            },
        )

        registerUserGraph(
            context = context,
            activity = activity,
            onBackClick = onBackClick,
        )

        homeGraph(
            context = context,
            activity = activity,
//            onBackPressedDispatcher = onBackPressedDispatcher,
            factory = EntryPointAccessors.fromActivity(
                context as Activity, MainActivity.ViewModelFactoryProvider::class.java
            ).homeViewModelFactory(),
            myUserId = myUserId,
            onNavigateToChat = { backStackEntry, s ->
                onNavigateToDestination(
                    backStackEntry,
                    ChatDestination,
                    ChatDestination.createNavigationRoute(s)
                )
            },
            onNavigateToLogin = {backStackEntry->
                onNavigateToDestination(backStackEntry, LoginDestination, LoginDestination.route)
            },
            onNavigateToSettings = {backStackEntry ->
                onNavigateToDestination(backStackEntry, SettingsDestination, LoginDestination.route)

            },

        )

        chatGraph(
            context = context,
            activity = activity,
            factory = EntryPointAccessors.fromActivity(
                context as Activity, MainActivity.ViewModelFactoryProvider::class.java
            ).chatViewModelFactory(),
            onBackClicked = onBackClick
        )

        settingsGraph(
            context, activity, onBackClick
        )
    }
}