package com.android.chatmeup.ui.screens.homescreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.android.chatmeup.R
import com.android.chatmeup.data.db.entity.UserInfo
import com.android.chatmeup.data.model.ChatWithUserInfo
import com.android.chatmeup.ui.screens.homescreen.components.BottomSheetScreen
import com.android.chatmeup.ui.screens.homescreen.components.ChatListItem
import com.android.chatmeup.ui.screens.homescreen.components.CmuSearchTextField
import com.android.chatmeup.ui.screens.homescreen.components.HomeBottomBar
import com.android.chatmeup.ui.screens.homescreen.components.HomeTopBar
import com.android.chatmeup.ui.screens.homescreen.components.SheetLayout
import com.android.chatmeup.ui.screens.homescreen.viewmodel.HomeViewModel
import com.android.chatmeup.ui.screens.homescreen.viewmodel.homeViewModelProvider
import com.android.chatmeup.util.SharedPreferencesUtil
import com.google.accompanist.pager.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    context: Context,
    activity: Activity?,
    factory: HomeViewModel.Factory,
    myUserId: String,
    onNavigateToChat: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSettings: () -> Unit,
){
    val myUserID = SharedPreferencesUtil.getUserID(context)

    val viewModel: HomeViewModel = homeViewModelProvider(
        factory = factory,
        myUserId = myUserID!!
    )

    val chatsList by viewModel.chatsList.observeAsState()

    val notificationsList by viewModel.notificationListWithUserInfo.observeAsState()

    val addContactEventState by viewModel.addContactEventState.collectAsState()

    var searchText by remember { mutableStateOf("") }

    val pagerState = rememberPagerState(1)

    val scope = rememberCoroutineScope()

    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden,
            confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded }
        )

    var currentBottomSheet: BottomSheetScreen by remember{
        mutableStateOf(BottomSheetScreen.AddContact)
    }

    var newContactEmail by remember {
        mutableStateOf("")
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
                        myUserId = myUserId,
                        onNavigateToChat = onNavigateToChat
                    )

                    2 -> Text(text = "More")
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
            Spacer(modifier = Modifier.height(25.dp))
            if(!list.isNullOrEmpty()){
                ChatList(
                    list = list,
                    myUserId = myUserId,
                    onNavigateToChat = onNavigateToChat
                ) }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatList(
    list: MutableList<ChatWithUserInfo>,
    myUserId: String,
    onNavigateToChat: (String) -> Unit
){
//    list?.sortBy { it.mChat.lastMessage.epochTimeMs }
    LazyColumn(
        modifier = Modifier.padding(start = 25.dp, end = 25.dp),
        verticalArrangement = Arrangement.spacedBy(25.dp)
    ){
        repeat(list.size){
            item {
                ChatListItem(
                    myUserId = myUserId,
                    item = list[it],
                    onNavigateToChat = onNavigateToChat
                )
            }
        }
    }
}