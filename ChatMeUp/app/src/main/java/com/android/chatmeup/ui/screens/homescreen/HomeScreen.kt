package com.android.chatmeup.ui.screens.homescreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.chatmeup.R
import com.android.chatmeup.ui.screens.components.CmuInputTextField
import com.android.chatmeup.ui.screens.homescreen.model.ContactListItem
import com.android.chatmeup.ui.theme.brand_color
import com.android.chatmeup.ui.theme.cmuDarkBlue
import com.android.chatmeup.ui.theme.cmuOffWhite
import com.android.chatmeup.ui.theme.neutral_disabled
import com.android.chatmeup.ui.theme.seed
import com.android.chatmeup.ui.theme.success_green
import com.fredrikbogg.android_chat_app.data.model.ChatWithUserInfo
import com.google.accompanist.pager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    context: Context,
    activity: Activity?,
    viewModel: HomeViewModel = hiltViewModel(),
){
    val chatsList by viewModel.chatsList.observeAsState()

    var searchText = remember { mutableStateOf(TextFieldValue("")) }

    val pagerState = rememberPagerState(1)

    val scope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetContent ={}
    ){
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                when (pagerState.currentPage) {
                    0 ->
                        HomeTopBar(
                            Modifier,
                            "Contacts",
                            {
                                IconButton(onClick = { }) {
                                    Icon(
                                        modifier = Modifier.size(22.dp),
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "icon.name",
                                    )
                                }
                            }
                        )

                    1 ->
                        HomeTopBar(
                            Modifier,
                            "Chats",
                            {
                                IconButton(onClick = { }) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_new_chat),
                                        contentDescription = "icon.name",
                                    )
                                }
                            }
                        )

                    2 ->
                        HomeTopBar(
                            Modifier,
                            "More",
                        )
                }
            },
            bottomBar = {
                HomeBottomBar(
                    pagerState = pagerState,
                    scope = scope
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                count = 3
            ) { page ->
                when (page) {
                    0 -> Text(text = "Contacts")
                    1 -> ChatsScreen(
                        modifier = Modifier.padding(it),
                        searchTextValue = searchText,
                        onSearchTextValueChanged = { searchText.value = it },
                        list = chatsList
                    )

                    2 -> Text(text = "More")
                }
            }

        }
    }
}

@Composable
fun ChatsScreen(
    modifier: Modifier = Modifier,
    searchTextValue: MutableState<TextFieldValue>,
    onSearchTextValueChanged: (TextFieldValue) -> Unit,
    list: MutableList<ChatWithUserInfo>?
){
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column() {
            CmuSearchTextField(
                searchTextValue = searchTextValue,
                onSearchTextValueChanged = onSearchTextValueChanged,
            )
            Spacer(modifier = Modifier.height(25.dp))
            ChatList(list = list)
        }
    }
}

@Composable
fun ChatList(list: MutableList<ChatWithUserInfo>?){
    list?.sortBy { it.mChat.lastMessage.epochTimeMs }
    LazyColumn(
        modifier = Modifier.padding(start = 25.dp, end = 25.dp),
        verticalArrangement = Arrangement.spacedBy(25.dp)
    ){
        repeat(3){
            item {
                ChatListItem(item = null)
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListItem(
    modifier: Modifier = Modifier,
    item: ChatWithUserInfo?
){
    Row(modifier = modifier) {
        BadgedBox(
            modifier = Modifier
                .size(60.dp),
//                .clip(RoundedCornerShape(30))
//                    .clipToBounds()
            badge = {
                Card(
                    modifier = Modifier
                        .size(20.dp)
                        .offset(x = (-10).dp, y = (10).dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ){
                    Icon(
                        modifier = Modifier
//                        .align(Alignment.TopEnd)
                            .padding(2.dp),
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        tint = success_green
                    )
                }
            },
        ){
            Image(
                modifier = Modifier
                    .clip(RoundedCornerShape(30))
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                painter = painterResource(id = R.drawable.fine_lady_profile_pic),
                contentDescription = "Profile picture"
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "Joshua Owolabi",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "How are you",
                overflow = TextOverflow.Ellipsis,
                color = neutral_disabled,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Today",
                color = neutral_disabled,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = brand_color
                )
            ) {
                Text(
                    text = "12",
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

@OptIn( ExperimentalComposeUiApi::class)
@Composable
fun CmuSearchTextField(
    searchTextValue: MutableState<TextFieldValue>,
    onSearchTextValueChanged: (TextFieldValue) -> Unit
) {
    val keyboardState = LocalSoftwareKeyboardController.current
    CmuInputTextField(
        label = "",
        placeholder = "Search Chats",
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
        },
        text = searchTextValue,
        onValueChanged = onSearchTextValueChanged,
        trailingIcon =
        {
            Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel Search")
        },
        onDone = { keyboardState?.hide() },
    )
}

@Preview
@Composable
fun HomeTopBar(modifier: Modifier = Modifier, title: String = "Chats", vararg icons: @Composable () -> Unit = emptyArray()){
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(top = 15.dp, start = 30.dp, end = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineSmall,
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
        color = MaterialTheme.colorScheme.background
    ){
        Row(
            modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 25.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                isItem = pagerState.currentPage == 0,
                label = "Contacts",
                icon = ImageVector.Companion.vectorResource(id = R.drawable.ic_contacts_svg),
                onItemClicked = {
                    scope.launch{
                        pagerState.animateScrollToPage(0)
                    }
                }
            )
            BottomBarItem(
                isItem = pagerState.currentPage == 1,
                label = "Chats",
                icon = ImageVector.Companion.vectorResource(id = R.drawable.ic_message_svg),
                onItemClicked = {
                    scope.launch{
                        pagerState.animateScrollToPage(1)
                    }
                }
            )
            BottomBarItem(
                isItem = pagerState.currentPage == 2,
                label = "More",
                icon = ImageVector.Companion.vectorResource(id = R.drawable.ic_more_svg),
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
                        fontWeight = FontWeight.Bold
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
                        .size(32.dp)
                )
            }
        }
    }
}
