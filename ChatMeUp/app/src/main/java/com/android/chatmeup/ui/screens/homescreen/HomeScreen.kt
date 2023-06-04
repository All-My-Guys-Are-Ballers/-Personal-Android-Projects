package com.android.chatmeup.ui.screens.homescreen

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.chatmeup.R
import com.android.chatmeup.data.db.firebase_db.entity.UserInfo
import com.android.chatmeup.data.model.ChatWithUserInfo
import com.android.chatmeup.ui.screens.components.ProfilePicture
import com.android.chatmeup.ui.screens.homescreen.components.BottomSheetScreen
import com.android.chatmeup.ui.screens.homescreen.components.ChatListItem
import com.android.chatmeup.ui.screens.homescreen.components.CmuSearchTextField
import com.android.chatmeup.ui.screens.homescreen.components.HomeBottomBar
import com.android.chatmeup.ui.screens.homescreen.components.HomeTopBar
import com.android.chatmeup.ui.screens.homescreen.components.SheetLayout
import com.android.chatmeup.ui.screens.homescreen.viewmodel.HomeViewModel
import com.android.chatmeup.ui.screens.homescreen.viewmodel.homeViewModelProvider
import com.android.chatmeup.ui.theme.md_theme_dark_background
import com.android.chatmeup.ui.theme.md_theme_light_background
import com.android.chatmeup.ui.theme.neutral_disabled
import com.google.accompanist.pager.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    context: Context,
    activity: Activity?,
    factory: HomeViewModel.Factory,
    onNavigateToChat: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
){
    val systemUiController = rememberSystemUiController()
    //change status bar color anytime we change light mode or dark mode

    val viewModel: HomeViewModel = homeViewModelProvider(
        factory = factory,
        myUserId = ""
    )

    val myUserInfo by viewModel.myUpdatedInfo.observeAsState()

    val chatsList by viewModel.chatsList.observeAsState()

    val notificationsList by viewModel.notificationListWithUserInfo.observeAsState()

    val addContactEventState by viewModel.addContactEventState.collectAsState()

    var searchText by rememberSaveable { mutableStateOf("") }

    var selectedImageTitle by rememberSaveable { mutableStateOf("Profile Picture") }

    var selectedImageUri by rememberSaveable { mutableStateOf(Uri.parse(myUserInfo?.profileImageUrl
        ?: "")) }

    val pagerState = rememberPagerState(1)

    val scope = rememberCoroutineScope()

    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )

    var currentBottomSheet: BottomSheetScreen by remember{
        mutableStateOf(BottomSheetScreen.AddContact)
    }

    var newContactEmail by remember {
        mutableStateOf("")
    }

    val isDarkTheme = isSystemInDarkTheme()

    LaunchedEffect(modalBottomSheetState.isVisible){
        if(!(modalBottomSheetState.isVisible && currentBottomSheet == BottomSheetScreen.ProfileImage)){
            systemUiController.setSystemBarsColor(
                color = if (isDarkTheme) md_theme_dark_background else md_theme_light_background,
                darkIcons = !isDarkTheme
            )
        }
        else{
            systemUiController.setSystemBarsColor(Color.Black, darkIcons = false)
        }
    }

    LaunchedEffect(notificationsList){
        if(notificationsList.isNullOrEmpty()){
            scope.launch{ modalBottomSheetState.hide() }
        }
    }

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
        sheetContent = {
            SheetLayout(
                currentScreen = currentBottomSheet,
                context = context,
                activity = activity,
                notificationsList = notificationsList as List<UserInfo>?,
                viewModel = viewModel,
                onAddContactClicked =
                {
                    viewModel.onAddContactEventTriggered(
                        event = HomeViewModel.AddContactEvents.Loading,
                        context = context,
                        activity = activity,
                        email = newContactEmail,
                    )
                },
                newContactEmail = newContactEmail,
                onNewContactEmailChanged = {newContactEmail= it},
                addContactEventState = addContactEventState,
                selectedImageTitle = selectedImageTitle,
                selectedImageUri = selectedImageUri,
                onCloseImage = {
                    systemUiController.setSystemBarsColor(
                        color = if (isDarkTheme) md_theme_dark_background else md_theme_light_background,
                        darkIcons = !isDarkTheme
                    )
                    scope.launch{
                        modalBottomSheetState.hide()
                    }
                }
            )
        }
    ){
        val keyboardController = LocalSoftwareKeyboardController.current
        if(!modalBottomSheetState.isVisible){
            keyboardController?.hide()
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                when (pagerState.currentPage) {
                    0 ->
                        HomeTopBar(
                            Modifier,
                            "Contacts",
                            {
                                IconButton(onClick = {
                                    Firebase.auth.signOut()
                                    onNavigateToLogin()
                                }) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
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
                                IconButton(
                                    onClick = {
                                        currentBottomSheet = BottomSheetScreen.AddContact
                                        scope.launch {
                                            modalBottomSheetState.show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_new_chat),
                                        contentDescription = "icon.name",
                                    )
                                }
                            },
                            {
                                if (!notificationsList.isNullOrEmpty()){
                                    IconButton(
                                        onClick = {
                                            currentBottomSheet = BottomSheetScreen.RequestsList
                                            scope.launch {
                                                modalBottomSheetState.show()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(20.dp),
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = "icon.name",
                                        )
                                    }
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
                        onSearchTextValueChanged = { searchText = it },
                        list = chatsList,
                        myUserId = viewModel.myUserId,
                        onProfileImageClicked = {userInfo: UserInfo ->
                            selectedImageTitle = userInfo.displayName
                            selectedImageUri = Uri.parse(userInfo.profileImageUrl)
                            currentBottomSheet = BottomSheetScreen.ProfileImage
                            systemUiController.setSystemBarsColor(Color.Black, darkIcons = false)
                            scope.launch {
                                modalBottomSheetState.show()
                            }
                        },
                        onNavigateToChat = onNavigateToChat,
                    )

                    2 -> MoreScreen(
                        modifier = Modifier.padding(it),
                        myUserInfo = myUserInfo,
                        onSignOutClicked = {
                            viewModel.logout()
                            onNavigateToLogin()
                        },
                        onNavigateToEditProfile = onNavigateToEditProfile
                    )
                }
            }

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatsScreen(
    modifier: Modifier = Modifier,
    searchTextValue: String,
    onSearchTextValueChanged: (String) -> Unit,
    onProfileImageClicked: (UserInfo) -> Unit,
    list: MutableList<ChatWithUserInfo>?,
    myUserId: String,
    onNavigateToChat: (String) -> Unit
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
            Spacer(modifier = Modifier.height(10.dp))
            if(!list.isNullOrEmpty()){
                val sortedList = list.sortedByDescending { it.mChat.lastMessage.epochTimeMs }
                ChatList(
                    list = sortedList,
                    myUserId = myUserId,
                    onNavigateToChat = onNavigateToChat,
                    onProfileImageClicked = onProfileImageClicked,
                )
            }
        }
    }
}

@Composable
fun MoreScreen(
    modifier: Modifier = Modifier,
    myUserInfo: UserInfo?,
    onSignOutClicked: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
){
    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 20.dp, start = 20.dp, end = 0.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column() {
            myUserInfo?.let {
                UserInfoWithProfilePicture(myUserInfo = it) {
                    onNavigateToEditProfile()
                }
            }
            Spacer(modifier = Modifier.height(25.dp))
            MoreItem() {
                
            }
            MoreItem(Icons.Default.Security, "Security") {
                
            }
            MoreItem(Icons.Default.Logout, "Sign Out") {
                onSignOutClicked()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatList(
    list: List<ChatWithUserInfo>,
    myUserId: String,
    onNavigateToChat: (String) -> Unit,
    onProfileImageClicked: (UserInfo) -> Unit
){
    LazyColumn(
        modifier = Modifier.padding(start = 25.dp, end = 25.dp),
//        verticalArrangement = Arrangement.spacedBy(25.dp)
    ){
        repeat(list.size){
            item {
                ChatListItem(
                    modifier = Modifier.padding(vertical = 10.dp),
                    myUserId = myUserId,
                    item = list[it],
                    onNavigateToChat = onNavigateToChat,
                    onProfileImageClicked = {
                        onProfileImageClicked(list[it].mUserInfo)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoWithProfilePicture(
    myUserInfo: UserInfo,
    onNavigateToEditProfile: () -> Unit,
){
    Card(onClick = {
        onNavigateToEditProfile()
    },
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row() {
            ProfilePicture(
                imageUrl = myUserInfo.profileImageUrl,
                size = 60.dp,
                shape = CircleShape
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = myUserInfo.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = myUserInfo.email,
                    overflow = TextOverflow.Ellipsis,
                    color = neutral_disabled,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
            IconButton(onClick = { onNavigateToEditProfile() }) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "",
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreItem(
    leadingIcon: ImageVector = Icons.Outlined.Person,
    text: String = "Account",
    onClick: () -> Unit,
){
    Card(
        onClick = { onClick() },
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(27.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = text,
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
            IconButton(onClick = { onClick() }) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "",
                    modifier = Modifier.size(15.dp)
                )
            }

        }
    }
}