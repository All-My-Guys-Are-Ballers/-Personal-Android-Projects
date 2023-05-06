package com.android.chatmeup.ui.screens.homescreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.chatmeup.R
import com.android.chatmeup.data.db.entity.UserInfo
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.ui.screens.components.CmuDarkButton
import com.android.chatmeup.ui.screens.components.CmuInputTextField
import com.android.chatmeup.ui.screens.components.ProfilePicture
import com.android.chatmeup.ui.theme.brand_color
import com.android.chatmeup.ui.theme.cmuDarkBlue
import com.android.chatmeup.ui.theme.cmuOffWhite
import com.android.chatmeup.ui.theme.md_theme_dark_onPrimaryContainer
import com.android.chatmeup.ui.theme.neutral_disabled
import com.android.chatmeup.ui.theme.seed
import com.android.chatmeup.util.isEmailValid
import com.fredrikbogg.android_chat_app.data.model.ChatWithUserInfo
import com.google.accompanist.pager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    context: Context,
    activity: Activity?,
    factory: HomeViewModel.Factory,
    myUserId: String,
){
    val viewModel: HomeViewModel = homeViewModelProvider(
        factory = factory,
        myUserId = myUserId
    )

    val chatsList by viewModel.chatsList.observeAsState()

    val notificationsList by viewModel.notificationListWithUserInfo.observeAsState()

    val addContactEventState by viewModel.addContactEventState.collectAsState()

    val searchText = remember { mutableStateOf(TextFieldValue("")) }

    val pagerState = rememberPagerState(1)

    val scope = rememberCoroutineScope()

    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden,
            confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded }
        )

    var currentBottomSheet: BottomSheetScreen by remember{
        mutableStateOf(BottomSheetScreen.AddContact)
    }

    val newContactEmail = remember {
        mutableStateOf(TextFieldValue(""))
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
                        email = newContactEmail.value.text,
                        errorMsg = "User does not have a ChatMeUp Account"
                    )
                },
                newContactEmail = newContactEmail,
                onNewContactEmailChanged = {newContactEmail.value = it},
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
            if(!list.isNullOrEmpty()){ ChatList(list = list) }
        }
    }
}

@Composable
fun ChatList(list: MutableList<ChatWithUserInfo>){
//    list?.sortBy { it.mChat.lastMessage.epochTimeMs }
    LazyColumn(
        modifier = Modifier.padding(start = 25.dp, end = 25.dp),
        verticalArrangement = Arrangement.spacedBy(25.dp)
    ){
        repeat(list.size){
            item {
                ChatListItem(item = list[it])
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListItem(
    modifier: Modifier = Modifier,
    item: ChatWithUserInfo
){
    Row(modifier = modifier) {
        ProfilePicture(
            imageId = R.drawable.fine_lady_profile_pic,
            isOnline = true,
            size = 60.dp
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.mUserInfo.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.mChat.lastMessage.text,
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
                        imageId = R.drawable.fine_lady_profile_pic,
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
    newContactEmail: MutableState<TextFieldValue>,
    onNewContactEmailChanged: (TextFieldValue) -> Unit,
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
                if(newContactEmail.value.text.isEmailValid()){
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
    addContactEventState: AddContactEventState,
    viewModel: HomeViewModel,
    notificationsList: List<UserInfo>?,
    onAddContactClicked: () -> Unit,
    newContactEmail: MutableState<TextFieldValue>,
    onNewContactEmailChanged: (TextFieldValue) -> Unit,
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
    }
}


sealed class BottomSheetScreen {
    object AddContact: BottomSheetScreen()
    object RequestsList: BottomSheetScreen()
}