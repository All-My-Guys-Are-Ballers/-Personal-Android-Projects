package com.android.chatmeup.ui.screens.loginscreen

import android.app.Activity
import android.content.Context
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.android.chatmeup.datastore.CmuDataStoreRepository
import com.android.chatmeup.navigation.CmuNavigationDestination

object LoginDestination : CmuNavigationDestination {
    override val route = "login_route"
    override val destination = "login_destination"
    override val shouldPopStack = false
}

fun NavGraphBuilder.loginGraph(
    context: Context,
    activity: Activity?,
    onClickRegister: (NavBackStackEntry) -> Unit,
    onLoggedIn: (NavBackStackEntry) -> Unit,
) {
    composable(
        route = LoginDestination.route
    ) { navBackStackEntry ->
        LoginScreen(
            context = context,
            activity = activity,
            onLoggedIn = {
                onLoggedIn(navBackStackEntry)
            },
            onClickRegister = {
                onClickRegister(navBackStackEntry)
            },
        )
    }
}