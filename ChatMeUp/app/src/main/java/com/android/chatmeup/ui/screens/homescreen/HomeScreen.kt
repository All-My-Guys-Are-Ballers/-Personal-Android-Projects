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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.chatmeup.R
import com.android.chatmeup.ui.screens.components.CmuInputTextField
import com.android.chatmeup.ui.theme.cmuDarkBlue
import com.android.chatmeup.ui.theme.cmuOffWhite
import com.fredrikbogg.android_chat_app.data.model.ChatWithUserInfo
import com.google.accompanist.pager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            when(pagerState.currentPage) {
                0 ->
                    HomeTopBar(
                        Modifier,
                        "Contacts",
                        Icons.Filled.Add
                    )
                1 ->
                    HomeTopBar(
                        Modifier,
                        "Chats",
                        ImageVector.vectorResource(id = R.drawable.ic_new_chat)
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
        ) }
    ) {
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            count = 3
        ){page ->
            when(page){
                0 -> Text(text = "Contacts")
                1 -> ContactsScreen(
                    modifier = Modifier.padding(it),
                    searchTextValue = searchText,
                    onSearchTextValueChanged = {searchText.value = it},
                    list = chatsList
                )
                2 -> Text(text = "More")
            }
        }

    }
}

@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    searchTextValue: MutableState<TextFieldValue>,
    onSearchTextValueChanged: (TextFieldValue) -> Unit,
    list: MutableList<ChatWithUserInfo>?
){
    Surface(modifier = modifier.fillMaxSize()) {
        Column() {
            CmuSearchTextField(
                searchTextValue = searchTextValue,
                onSearchTextValueChanged = onSearchTextValueChanged,
            )
            Spacer(modifier = Modifier.height(10.dp))
            ContactList(list = list)
        }
    }
}

@Composable
fun ContactList(list: MutableList<ChatWithUserInfo>?){
    list?.sortBy { it.mChat.lastMessage.epochTimeMs }
    LazyColumn(){
        repeat(3){
            item {
                ContactListItem(null)
            }

        }
    }
}

@Composable
fun ContactListItem(item: ChatWithUserInfo?){
    Surface(){
        Row() {
            Box(modifier = Modifier.size(20.dp)){
                Image(painter = painterResource(id = R.drawable.login_image), contentDescription = "Profile picture")
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Joshua Owolabi"
                )
                Text(
                    text = "How are you"
                )
            }
            Column() {
                Text(
                    text = "Today"
                )
                Card() {
                    Text(text = "1")
                }

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
//    OutlinedTextField(
//        modifier = modifier.fillMaxWidth(),
//        value = searchTextValue,
//        onValueChange = onSearchTextValueChanged,
//        placeholder = {
//            Text(text = "Search")
//        },
//        leadingIcon = {
//            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
//        },
//        singleLine = true,
//        textStyle = MaterialTheme.typography.bodyMedium,
////        colors = TextFieldDefaults.colors(
////            focusedContainerColor = containerColor,
////            unfocusedContainerColor = containerColor,
////            disabledContainerColor = containerColor,
////        ),
//        shape = RoundedCornerShape(20)
////        placeholder = "Search"
//    )
}

@Composable
fun HomeTopBar(modifier: Modifier = Modifier, title: String, vararg icons: ImageVector){
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(20.dp)) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        for(icon in icons){
            Icon(
                imageVector = icon,
                contentDescription = icon.name,
            )
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
        shape = RoundedCornerShape(4.dp)
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
