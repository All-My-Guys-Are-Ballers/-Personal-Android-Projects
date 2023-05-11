package com.android.chatmeup.ui.screens.homescreen

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.android.chatmeup.navigation.CmuNavigationDestination

object HomeDestination : CmuNavigationDestination {
    override val route = "chat_list_route"
    override val destination = "chat_list_destination"
    override val shouldPopStack = false
}

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.homeGraph(
    context: Context,
    activity: Activity?,
    factory: HomeViewModel.Factory,
    myUserId: String,
    onNavigateToChat: (NavBackStackEntry, String) -> Unit,
    onNavigateToLogin: (NavBackStackEntry) -> Unit,
) {
    composable(
        route = HomeDestination.route
    ) { navBackStackEntry ->
        HomeScreen(
            context = context,
            activity = activity,
            factory = factory,
            myUserId = myUserId,
            onNavigateToChat = {
                onNavigateToChat(navBackStackEntry, it)
            },
            onNavigateToLogin = {
                onNavigateToLogin(navBackStackEntry)
            }
        )
    }
}