package com.android.chatmeup.ui.screens.homescreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.chatmeup.R
import com.android.chatmeup.ui.theme.cmuBlack
import com.android.chatmeup.ui.theme.cmuWhite

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    context: Context,
    activity: Activity?,
    viewModel: HomeViewModel = hiltViewModel(),
){
    val homeEventState by viewModel.homeEventState.collectAsState()

    Scaffold(
        bottomBar = {
            HomeBottomBar(
                homeEventState = homeEventState,
                onChatsClicked = {
                    viewModel.onEventTriggered(HomeViewModel.HomeEvents.ChatListEvent)
                },
                onContactClicked = {
                    viewModel.onEventTriggered(HomeViewModel.HomeEvents.ContactListEvent)
                },
                onMoreClicked = {
                    viewModel.onEventTriggered(HomeViewModel.HomeEvents.MoreEvent)
                },
        ) }
    ) {

    }
}

@Composable
fun HomeTopBar(
){

}

@Composable
fun HomeBottomBar(modifier: Modifier = Modifier,
                  homeEventState: HomeEventState,
                  onChatsClicked: () -> Unit,
                  onContactClicked: () -> Unit,
                  onMoreClicked: () -> Unit
){
    Surface(
        elevation = 4.dp,
        color = if (isSystemInDarkTheme()) Color.Black else Color.White
    ){
        Row(
            modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 25.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(100.dp),
                contentAlignment = Alignment.Center
            ) {
                if (homeEventState == HomeEventState.CONTACTS) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Contacts",
                            style = MaterialTheme.typography.body2,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Icon(
                            imageVector = Icons.Rounded.Circle,
                            contentDescription = "",
                            tint = if (isSystemInDarkTheme()) cmuWhite else cmuBlack,
                            modifier = Modifier.size(4.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = ImageVector.Companion.vectorResource(id = R.drawable.ic_contacts_svg),
                        contentDescription = "",
                        tint = if (isSystemInDarkTheme()) cmuWhite else cmuBlack,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onContactClicked() }
                    )
                }
            }
            Box(
                modifier = Modifier.width(100.dp),
                contentAlignment = Alignment.Center
            ) {
                if (homeEventState == HomeEventState.CHATS) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Chats",
                            style = MaterialTheme.typography.body2,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Icon(
                            imageVector = Icons.Rounded.Circle,
                            contentDescription = "",
                            tint = if (isSystemInDarkTheme()) cmuWhite else cmuBlack,
                            modifier = Modifier.size(4.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = ImageVector.Companion.vectorResource(id = R.drawable.ic_message_svg),
                        contentDescription = "",
                        tint = if (isSystemInDarkTheme()) cmuWhite else cmuBlack,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onChatsClicked() }
                    )
                }
            }
            Box(
                modifier = Modifier.width(100.dp),
                contentAlignment = Alignment.Center
            ) {
                if (homeEventState == HomeEventState.MORE) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "More",
                            style = MaterialTheme.typography.body2,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Icon(
                            imageVector = Icons.Rounded.Circle,
                            contentDescription = "",
                            tint = if (isSystemInDarkTheme()) cmuWhite else cmuBlack,
                            modifier = Modifier.size(4.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = ImageVector.Companion.vectorResource(id = R.drawable.ic_more_svg),
                        contentDescription = "",
                        tint = if (isSystemInDarkTheme()) cmuWhite else cmuBlack,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onMoreClicked() }
                    )
                }
            }
        }
    }
}
