package com.android.chatmeup.ui.screens.chat.views

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.android.chatmeup.R
import com.android.chatmeup.data.db.firebase_db.entity.ChatInfo
import com.android.chatmeup.data.db.firebase_db.entity.Message
import com.android.chatmeup.data.db.firebase_db.entity.UserInfo
import com.android.chatmeup.ui.cmutoast.CmuToast
import com.android.chatmeup.ui.cmutoast.CmuToastDuration
import com.android.chatmeup.ui.cmutoast.CmuToastStyle
import com.android.chatmeup.ui.screens.chat.data.ChatState
import com.android.chatmeup.ui.screens.chat.viewmodel.ChatViewModel
import com.android.chatmeup.ui.screens.chat.viewmodel.chatViewModelProvider
import com.android.chatmeup.ui.screens.components.CmuOutlinedTextField
import com.android.chatmeup.ui.screens.components.ProfilePicture
import com.android.chatmeup.ui.screens.components.TextImage
import com.android.chatmeup.ui.screens.components.UploadFileOptionDialog
import com.android.chatmeup.ui.screens.components.UploadImageScreen
import com.android.chatmeup.ui.theme.neutral_disabled
import com.android.chatmeup.ui.theme.seed
import com.android.chatmeup.util.createTempImageFile
import com.android.chatmeup.util.epochToHoursAndMinutes
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(
    context: Context,
    activity: Activity?,
    factory: ChatViewModel.Factory,
    chatId: String,
    userId: String,
    otherUserId: String,
    onBackClicked: () -> Unit,
    noOfUnreadMessages: String
) {
    val viewModel = chatViewModelProvider(
        chatId = chatId,
        myUserId = userId,
        otherUserId = otherUserId,
        factory = factory
    )
    val chatInfo by viewModel.chatInfo.observeAsState()
    val otherUserInfo by viewModel.otherUser.observeAsState()

    val messageList by viewModel.messagesList.observeAsState()

    val newMessageText by viewModel.newMessageText.observeAsState()

    val lazyListState = rememberLazyListState(messageList?.size?.minus(chatInfo?.no_of_unread_messages!!) ?: 0)

    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden,
            confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded }
        )

    var newPhotoURI: Uri? by rememberSaveable {
        mutableStateOf(null)
    }

    var chatState by rememberSaveable {
        mutableStateOf(ChatState.CHAT)
    }


    val scope = rememberCoroutineScope()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            // Retrieve the captured image URI from the camera
//            photoURI = newPhotoURI
            scope.launch{ modalBottomSheetState.hide() }
            chatState = ChatState.UPLOAD_IMAGE
        }
    }

    val storageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            CmuToast.createFancyToast(
                context = context,
                activity = activity,
                title = "Image Upload",
                message = "You did not select any Image",
                style = CmuToastStyle.WARNING,
                duration = CmuToastDuration.SHORT
            )
        } else {
            newPhotoURI = uri
            Timber.tag(ContentValues.TAG).d("Photo URI: $newPhotoURI")
            scope.launch{ modalBottomSheetState.hide() }
            chatState = ChatState.UPLOAD_IMAGE
        }
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch the camera
            if (newPhotoURI != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.contentResolver.delete(newPhotoURI!!, null)
            }
            newPhotoURI = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                createTempImageFile(context)
            )
            cameraLauncher.launch(newPhotoURI)
        } else {
            // Permission denied, show an error message
            CmuToast.createFancyToast(context, activity = activity, "Camera","Permission denied", CmuToastStyle.ERROR, CmuToastDuration.SHORT)
        }
    }

    val requestStoragePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {

            storageLauncher.launch("image/*")
        } else {
            // Permission denied, show an error message
            CmuToast.createFancyToast(context, activity = activity, "Storage","Permission denied", CmuToastStyle.ERROR, CmuToastDuration.SHORT)
        }
    }

    LaunchedEffect(messageList){
        if(lazyListState.layoutInfo.totalItemsCount > noOfUnreadMessages.toInt()+1) {
//            lazyListState.animateScrollToItem(
//                lazyListState.layoutInfo.totalItemsCount - 1 - noOfUnreadMessages.toInt()
//            )
        }
    }

    when(chatState){
        ChatState.CHAT -> {
            ChatListScreen(
                onTakePicture = { requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                onUploadFromStorageClicked = { requestStoragePermissionLauncher.launch(
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
                ) },
                otherUserInfo = otherUserInfo,
                onBackClicked = onBackClicked,
                newMessageText = newMessageText,
                viewModel = viewModel,
                messageList = messageList,
                lazyListState = lazyListState,
                userId = userId,
                chatInfo = chatInfo,
                modalBottomSheetState = modalBottomSheetState,
                onAddItemClicked = {
                    scope.launch {
                        modalBottomSheetState.show()
                    }
                },
                newPhotoUri = newPhotoURI,
                context = context,
                activity = activity,
            )
        }
        ChatState.UPLOAD_IMAGE -> {
            newPhotoURI?.let {
                UploadImageScreen(
                    imageUri = it,
                    onUploadCancelled = {
                        chatState = ChatState.CHAT
                    },
                    messageText = newMessageText!!,
                    onValueChanged = {value ->
                        viewModel.newMessageText.value = value
                    },
                    onSendMessage = {
                        newPhotoURI?.let { uri ->
                            viewModel.sendMessagePressed(
                                context = context,
                                activity = activity,
                                newPhotoURI = uri
                            )
                        } ?: CmuToast.createFancyToast(
                            context = context,
                            activity = activity,
                            "Error",
                            "Unable to upload Image. Please try again",
                            CmuToastStyle.ERROR,
                            CmuToastDuration.SHORT
                        )
                        chatState = ChatState.CHAT
                    }

                )
            } ?: {
                CmuToast.createFancyToast(
                    context = context,
                    activity = activity,
                    "Error",
                    "Unable to upload Image. Please try again",
                    CmuToastStyle.ERROR,
                    CmuToastDuration.SHORT
                )
                chatState = ChatState.CHAT
            }
        }
    }



}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatListScreen(
    onTakePicture: () -> Unit,
    onUploadFromStorageClicked: () -> Unit,
    otherUserInfo: UserInfo?,
    onBackClicked: () -> Unit,
    newMessageText: String?,
    viewModel: ChatViewModel,
    messageList: MutableList<Message>?,
    lazyListState: LazyListState,
    userId: String,
    chatInfo: ChatInfo?,
    modalBottomSheetState: ModalBottomSheetState,
    onAddItemClicked: () -> Unit,
    newPhotoUri: Uri?,
    context: Context,
    activity: Activity?,
){
    val scope = rememberCoroutineScope()
    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContent = {
            UploadFileOptionDialog(
                title = "Upload Media",
                onTakePictureClicked = onTakePicture,
                onUploadFromStorageClicked = onUploadFromStorageClicked)
        }
        ){
        Scaffold(
            topBar = {
                ChatTopBar(
                    otherUserInfo = otherUserInfo,
                    onBackClicked = onBackClicked
                )
            },
            bottomBar = {
                ChatBottomBar(
                    messageText = newMessageText ?: "",
                    viewModel = viewModel,
                    onAddItemClicked = onAddItemClicked,
                    onSendMessagePressed = {
                        viewModel.sendMessagePressed(context,
                        activity, newPhotoUri)
//                        scope.launch {
//                            lazyListState.animateScrollToItem(index = lazyListState.layoutInfo.totalItemsCount-1)
//                        }
                    }
                )
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                if (!messageList.isNullOrEmpty()) {
                    LazyColumn(
                        state = lazyListState,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        repeat(messageList.size ?: 0) {
                            item {
                                MessageItem(
                                    myUserId = userId,
                                    message = messageList[it],
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
fun MessageItem(
    myUserId: String,
    message: Message,
){
    val isSender = myUserId == message.senderID

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
        horizontalArrangement = if(isSender) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 15.dp,
                topEnd = 15.dp,
                bottomStart = if(isSender) 15.dp else 0.dp,
                bottomEnd = if(isSender) 0.dp else 15.dp
            ),
            modifier = Modifier.padding(start = if(isSender)70.dp else 0.dp, end = if(!isSender)70.dp else 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = if(isSender) seed else MaterialTheme.colorScheme.background
            )
        ) {
            Column(
//                modifier = Modifier.padding(10.dp),
                horizontalAlignment = if (isSender) Alignment.End else Alignment.Start
            ) {
                if(message.imageUrl.isNotBlank()){
                    TextImage(
                        modifier = Modifier.padding(5.dp),
                        imageUri = Uri.parse(message.imageUrl)
                    ) {
                        // TODO :: Load Image Page
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = message.text,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(horizontal = 10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Left,
                    color = if(isSender) Color.White else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${epochToHoursAndMinutes(message.epochTimeMs)}${
                        if(isSender) {
                            if(message.seen) "•Read" else "•Sent"
                        } else ""
                    }",
                    modifier = Modifier.padding(horizontal = 10.dp),
                    color = neutral_disabled,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}

@Composable
fun ChatTopBar(otherUserInfo: UserInfo?,
               onBackClicked: () -> Unit,
){
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ){
        Row(
            modifier = Modifier.padding(bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBackClicked() }) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    "Back button"
                )
            }
            ProfilePicture(
                imageFile = otherUserInfo?.profileImageUrl ?: "",
                size = 40.dp,
                isOnline = otherUserInfo?.online ?: false
            )
            
            Spacer(modifier = Modifier.width(20.dp))

            otherUserInfo?.displayName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
fun ChatBottomBar(
    messageText: String,
    viewModel: ChatViewModel,
    onAddItemClicked: () -> Unit,
    onSendMessagePressed: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.wrapContentSize()) {
        Row(
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onAddItemClicked() }) {
                Icon(
                    modifier = Modifier.size(25.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = "Send Message button"
                )
            }

            var isSingleLine by remember {
                mutableStateOf(false)
            }
            var maxLines by remember {
                mutableStateOf(1)
            }
            CmuOutlinedTextField(
                value = messageText,
                onValueChange = {
                    viewModel.newMessageText.value = it
                },
//                    .height(40.dp),
                modifier = Modifier.weight(1f).heightIn(min = 40.dp, max = 100.dp),
                placeholder = {
                    Text(
                        text = "Message",
                        color = neutral_disabled,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterVertically)
                    ) },
                singleLine = isSingleLine,
                maxLines = 3,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    selectionColors = TextSelectionColors(
                        backgroundColor = neutral_disabled,
                        handleColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ),
            )

            IconButton(onClick = {
                onSendMessagePressed()
            }) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = ImageVector.Companion.vectorResource(id = R.drawable.ic_send_button),
                    contentDescription = "Send Message button",
                    tint = seed
                )
            }
        }
    }
}