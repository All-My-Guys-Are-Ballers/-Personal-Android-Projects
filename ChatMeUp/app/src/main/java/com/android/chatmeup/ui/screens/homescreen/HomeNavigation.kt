package com.android.chatmeup.ui.screens.homescreen

import android.app.Activity
import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.android.chatmeup.navigation.CmuNavigationDestination

object HomeDestination : CmuNavigationDestination {
    override val route = "chat_list_route"
    override val destination = "chat_list_destination"
    override val shouldPopStack = false
}

fun NavGraphBuilder.homeGraph(
    context: Context,
    activity: Activity?,
) {
    composable(
        route = HomeDestination.route
    ) { navBackStackEntry ->
        HomeScreen(
            context = context,
            activity = activity,
        )
    }
}