package com.android.chatmeup.ui.screens.homescreen.components

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.chatmeup.R
import com.android.chatmeup.data.db.entity.UserInfo
import com.android.chatmeup.data.model.ChatWithUserInfo
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.ui.screens.components.CmuDarkButton
import com.android.chatmeup.ui.screens.components.CmuInputTextField
import com.android.chatmeup.ui.screens.components.ImagePage
import com.android.chatmeup.ui.screens.components.ProfilePicture
import com.android.chatmeup.ui.screens.homescreen.viewmodel.AddContactEventState
import com.android.chatmeup.ui.screens.homescreen.viewmodel.HomeViewModel
import com.android.chatmeup.ui.theme.brand_color
import com.android.chatmeup.ui.theme.cmuDarkBlue
import com.android.chatmeup.ui.theme.cmuOffWhite
import com.android.chatmeup.ui.theme.md_theme_dark_onPrimaryContainer
import com.android.chatmeup.ui.theme.neutral_disabled
import com.android.chatmeup.ui.theme.seed
import com.android.chatmeup.util.epochToHoursAndMinutes
import com.android.chatmeup.util.isEmailValid
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListItem(
    modifier: Modifier = Modifier,
    myUserId: String,
    item: ChatWithUserInfo,
    onNavigateToChat: (String) -> Unit,
    onProfileImageClicked: () -> Unit
){
    Card(onClick = {
        onNavigateToChat("${item.mChat.info.id}%%%${myUserId}%%%${item.mUserInfo.id}")
    },
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(modifier = modifier) {
            ProfilePicture(
                modifier = Modifier.clickable {
                    onProfileImageClicked()
                },
                imageUrl = item.mUserInfo.profileImageUrl,
                isOnline = item.mUserInfo.online,
                size = 60.dp
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.mUserInfo.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.mChat.lastMessage.text,
                    overflow = TextOverflow.Ellipsis,
                    color = neutral_disabled,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = epochToHoursAndMinutes(item.mChat.lastMessage.epochTimeMs),
                    color = neutral_disabled,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(10.dp))
                if(!item.mChat.lastMessage.seen && item.mChat.lastMessage.senderID == item.mUserInfo.id){
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = brand_color
                        )
                    ) {
                        Text(
                            text = item.mChat.info.no_of_unread_messages.toString(),
                            modifier = Modifier.padding(
                                start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = seed
                        )
                    }
                }
            }
        }
    }
}

@OptIn( ExperimentalComposeUiApi::class)
@Composable
fun CmuSearchTextField(
    searchTextValue: String,
    onSearchTextValueChanged: (String) -> Unit
) {
    val keyboardState = LocalSoftwareKeyboardController.current
    CmuInputTextField(
        modifier = Modifier.height(60.dp),
        label = "",
        placeholder = "Search Chats",
        leadingIcon = {
            Icon(
//                modifier = Modifier.size(22.dp),
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = neutral_disabled
            )
        },
        text = searchTextValue,
        shape = RoundedCornerShape(10),
        onValueChanged = onSearchTextValueChanged,
//        trailingIcon =
//        {
//            Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel Search")
//        },
        onDone = { keyboardState?.hide() },
    )
}

@Preview
@Composable
fun HomeTopBar(modifier: Modifier = Modifier, title: String = "Chats", vararg icons: @Composable () -> Unit = emptyArray()){
    Row(modifier = modifier
        .fillMaxWidth()
        .height(50.dp)
        .padding(top = 0.dp, start = 30.dp, end = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )
        for(icon in icons){
            icon()
        }
    }
}
@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeBottomBar(modifier: Modifier = Modifier,
                  scope: CoroutineScope,
                  pagerState: PagerState,
){
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 10.dp,
        color = MaterialTheme.colorScheme.background
    ){
        Row(
            modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 25.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                isItem = pagerState.currentPage == 0,
                label = "Contacts",
                icon = ImageVector.vectorResource(id = R.drawable.ic_contacts_svg),
                onItemClicked = {
                    scope.launch{
                        pagerState.animateScrollToPage(0)
                    }
                }
            )
            BottomBarItem(
                isItem = pagerState.currentPage == 1,
                label = "Chats",
                icon = ImageVector.vectorResource(id = R.drawable.ic_message_svg),
                onItemClicked = {
                    scope.launch{
                        pagerState.animateScrollToPage(1)
                    }
                }
            )
            BottomBarItem(
                isItem = pagerState.currentPage == 2,
                label = "More",
                icon = ImageVector.vectorResource(id = R.drawable.ic_more_svg),
                onItemClicked = {
                    scope.launch{
                        pagerState.animateScrollToPage(2)
                    }
                }
            )
        }
    }
}

@Composable
fun BottomBarItem(
    modifier: Modifier = Modifier,
    isItem: Boolean,
    label: String,
    icon: ImageVector,
    onItemClicked : () -> Unit
){
    Surface(
        modifier = modifier.width(100.dp),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.background
    ){
        AnimatedVisibility(
            visible = isItem,
            enter = fadeIn(),
            exit = ExitTransition.None
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Icon(
                        imageVector = Icons.Rounded.Circle,
                        contentDescription = "",
                        tint = if (isSystemInDarkTheme()) cmuOffWhite else cmuDarkBlue,
                        modifier = Modifier.size(4.dp)
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = !isItem,
            enter = EnterTransition.None,
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null
                    ){onItemClicked()},
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "",
                    tint = if (isSystemInDarkTheme()) cmuOffWhite else cmuDarkBlue,
                    modifier = Modifier
                        .size(28.dp)
                )
            }
        }
    }
}

@Composable
fun RequestsList(
    viewModel: HomeViewModel,
    notificationsList: List<UserInfo>
){
    LazyColumn(
        modifier = Modifier
            .padding(25.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(25.dp)
    ){
        repeat(notificationsList.size){
            item {
                Row() {
                    ProfilePicture(
                        imageUrl = notificationsList[it].profileImageUrl,
                        isOnline = true,
                        size = 90.dp
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)){
                        Text(
                            text = notificationsList[it].displayName,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        Row(modifier = Modifier){
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = seed
                                ),
                                onClick = {
                                    viewModel.acceptNotificationPressed(notificationsList[it])
                                }
                            ) {
                                Text(
                                    text = "Confirm",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = md_theme_dark_onPrimaryContainer,
                                )
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                onClick = {
                                    viewModel.declineNotificationPressed(notificationsList[it])
                                }
                            ) {
                                Text(
                                    text = "Delete",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddContactDialog(
    context: Context,
    activity: Activity?,
    newContactEmail: String,
    onNewContactEmailChanged: (String) -> Unit,
    onAddContactClicked: () -> Unit,
    addContactEventState: AddContactEventState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        CmuInputTextField(
            label = "",
            placeholder = "Contact Email",
            text = newContactEmail,
            onValueChanged = onNewContactEmailChanged
        )
        CmuDarkButton(
            modifier = Modifier.padding(bottom = 20.dp),
            label = "Add New Contact",
            padding = PaddingValues(start = 30.dp, end =  30.dp),
            isLoading = addContactEventState == AddContactEventState.LOADING,
            onClick = {
                if(newContactEmail.isEmailValid()){
                    onAddContactClicked()
                }
                else{
                    CmuToast.createFancyToast(
                        context = context,
                        activity = activity,
                        title = "Email Error",
                        message = "Please input a valid email",
                        style = CmuToastStyle.ERROR,
                        duration = CmuToastDuration.SHORT
                    )
                }

            }
        )
    }
}

@Composable
fun SheetLayout(
    currentScreen: BottomSheetScreen,
    context: Context,
    activity: Activity?,
    selectedImageTitle: String,
    selectedImageUri: Uri,
    addContactEventState: AddContactEventState,
    viewModel: HomeViewModel,
    notificationsList: List<UserInfo>?,
    onAddContactClicked: () -> Unit,
    newContactEmail: String,
    onNewContactEmailChanged: (String) -> Unit,
    onCloseImage: () -> Unit
) {
    when(currentScreen){
        BottomSheetScreen.AddContact -> {
            AddContactDialog(
                context = context,
                activity = activity,
                newContactEmail = newContactEmail,
                onNewContactEmailChanged = onNewContactEmailChanged,
                addContactEventState = addContactEventState,
                onAddContactClicked = onAddContactClicked
            )
        }

        BottomSheetScreen.RequestsList -> {
            if (notificationsList != null) {
                RequestsList(
                    viewModel = viewModel,
                    notificationsList = notificationsList
                )
            }
        }

        BottomSheetScreen.ProfileImage -> {
            ImagePage(
                title = selectedImageTitle,
                imageUri = selectedImageUri
            ) {
                onCloseImage()
            }
        }
    }
}


sealed class BottomSheetScreen {
    object AddContact: BottomSheetScreen()
    object RequestsList: BottomSheetScreen()
    object ProfileImage: BottomSheetScreen()
}