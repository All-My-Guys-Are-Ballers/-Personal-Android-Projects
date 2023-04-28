package com.android.chatmeup.navigation

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.android.chatmeup.MainActivity
import com.android.chatmeup.data.datastore.CmuDataStoreRepository
import com.android.chatmeup.ui.screens.homescreen.HomeDestination
import com.android.chatmeup.ui.screens.homescreen.homeGraph
import com.android.chatmeup.ui.screens.loginscreen.loginGraph
import com.android.chatmeup.ui.screens.registeruserscreen.RegisterUserDestination
import com.android.chatmeup.ui.screens.registeruserscreen.registerUserGraph
import dagger.hilt.android.EntryPointAccessors

@Composable
fun CmuNavHost(
    context: Context,
    activity: Activity?,
    navController: NavHostController,
    onNavigateToDestination: (NavBackStackEntry, CmuNavigationDestination, String) -> Unit,
    startDestination: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
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
        )
    }
}