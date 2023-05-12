package com.android.chatmeup.ui.screens.chat.views

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.android.chatmeup.R
import com.android.chatmeup.data.db.entity.Message
import com.android.chatmeup.data.db.entity.UserInfo
import com.android.chatmeup.ui.screens.chat.viewmodel.ChatViewModel
import com.android.chatmeup.ui.screens.chat.viewmodel.chatViewModelProvider
import com.android.chatmeup.ui.screens.components.CmuInputTextField
import com.android.chatmeup.ui.screens.components.ProfilePicture
import com.android.chatmeup.ui.theme.neutral_disabled
import com.android.chatmeup.ui.theme.seed
import com.android.chatmeup.util.epochToHoursAndMinutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(
    context: Context,
    activity: Activity?,
    factory: ChatViewModel.Factory,
    chatId: String,
    userId: String,
    otherUserId: String,
    onBackClicked: () -> Unit
) {
    val viewModel = chatViewModelProvider(
        chatId = chatId,
        myUserId = userId,
        otherUserId = otherUserId,
        factory = factory
    )

    val otherUserInfo by viewModel.otherUser.observeAsState()

    val messageList by viewModel.messagesList.observeAsState()

    val newMessageText by viewModel.newMessageText.observeAsState()

    val lazyListState by viewModel.lazyListState.collectAsState()

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
                viewModel = viewModel
            )
        }
    ){paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.tertiaryContainer
        ){
            if (!messageList.isNullOrEmpty()){
                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    repeat(messageList?.size ?: 0) {
                        item{ 
                            MessageItem(
                                myUserId = userId,
                                message = messageList!![it]) 
                        }
                    }
                }
            }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessageItem(
    myUserId: String,
    message: Message
){
    val isSender = myUserId == message.senderID
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
        horizontalArrangement = if(isSender) Arrangement.End else Arrangement.Start
    ) {
        if(isSender){
            Spacer(modifier = Modifier.fillMaxWidth(0.2f))
        }
        Card(
            shape = RoundedCornerShape(
                topStartPercent = 25,
                topEndPercent = 25,
                bottomStartPercent = if(isSender) 25 else 0,
                bottomEndPercent = if(isSender) 0 else 25
            ),
            colors = CardDefaults.cardColors(
                containerColor = if(isSender) seed else MaterialTheme.colorScheme.background
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = if (isSender) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if(isSender) Color.White else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${epochToHoursAndMinutes(message.epochTimeMs)}${
                        if(isSender) {
                            if(message.seen) "•Read" else "•Sent"
                        } else ""
                    }",
                    color = if(isSender) Color.White else MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        if(!isSender){
            Spacer(modifier = Modifier.fillMaxWidth(0.2f))
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
                imageUrl = otherUserInfo?.profileImageUrl ?: "",
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
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    modifier = Modifier.size(25.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = "Send Message button"
                )
            }

            CmuInputTextField(
                label = "",
                placeholder = "Message",
                modifier = Modifier
                    .weight(1f),
                paddingValues = PaddingValues(),
                singleLine = false,
                maxLines = 3,
                text = messageText,
                shape = RoundedCornerShape(10),
                onValueChanged = {
                    viewModel.newMessageText.value = it
                },
//        trailingIcon =
//        {
//            Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel Search")
//        },
//                onDone = { keyboardState?.hide() },
            )

            IconButton(onClick = {
                viewModel.sendMessagePressed()
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