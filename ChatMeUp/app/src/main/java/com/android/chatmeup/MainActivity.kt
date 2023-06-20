package com.android.chatmeup

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.compose.rememberNavController
import com.android.chatmeup.data.Result
import com.android.chatmeup.data.datastore.CmuDataStoreRepository
import com.android.chatmeup.data.db.firebase_db.remote.FirebaseReferenceChildObserver
import com.android.chatmeup.data.db.firebase_db.repository.DatabaseRepository
import com.android.chatmeup.data.db.room_db.ChatMeUpDatabase
import com.android.chatmeup.ui.CmuApp
import com.android.chatmeup.ui.screens.chat.viewmodel.ChatViewModel
import com.android.chatmeup.ui.screens.homescreen.viewmodel.HomeViewModel
import com.android.chatmeup.ui.theme.ChatMeUpTheme
import com.android.chatmeup.ui.theme.md_theme_dark_background
import com.android.chatmeup.ui.theme.md_theme_light_background
import com.android.chatmeup.util.firebaseMessageToRoomMessage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val tag = MainActivity::class.java.simpleName
    private lateinit var chatMeUpApp: CmuApplication
    private val dbRepository = DatabaseRepository()
    private val fbReferenceChildObserver = FirebaseReferenceChildObserver()

    @Inject
    lateinit var cmuDataStoreRepository: CmuDataStoreRepository

    @Inject
    lateinit var chatMeUpDatabase: ChatMeUpDatabase

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface ViewModelFactoryProvider {
        fun homeViewModelFactory(): HomeViewModel.Factory
        fun chatViewModelFactory(): ChatViewModel.Factory

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        chatMeUpApp = applicationContext as CmuApplication

        Firebase.auth.currentUser?.let {  dbRepository.updateOnlineStatus(it.uid, true)}
        loadAndObserveAllNewMessages(fbReferenceChildObserver)

        setContent {
            val systemUiController = rememberSystemUiController()
            //change status bar color anytime we change light mode or dark mode
            val isDarkTheme = isSystemInDarkTheme()
            LaunchedEffect(isSystemInDarkTheme()) {
                systemUiController.setStatusBarColor(
                    color = if(isDarkTheme) md_theme_dark_background else md_theme_light_background,
                    darkIcons = !isDarkTheme
                )
            }
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
                val bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                view.updatePadding(bottom = bottom)
                insets
            }
            val navController = rememberNavController()
            ChatMeUpTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CmuApp(
                        newMessagesObserver = fbReferenceChildObserver,
                        chatMeUpApp = chatMeUpApp,
                        startObserving = {
                            loadAndObserveAllNewMessages(fbReferenceChildObserver)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        chatMeUpApp.setCurrentActivity(this)
        Firebase.auth.currentUser?.let {  dbRepository.updateOnlineStatus(it.uid, true)}
    }
    override fun onPause() {
        super.onPause()
        chatMeUpApp.setCurrentActivity(this)
        Firebase.auth.currentUser?.let {  dbRepository.updateOnlineStatus(it.uid, true)}
    }

//    override fun onStart() {
//        super.onStart()
//        fbReferenceChildObserver.start()
//    }
//    override fun onStop() {
//        super.onStop()
//        fbReferenceChildObserver.clear()
//    }

    private fun loadAndObserveAllNewMessages(observer: FirebaseReferenceChildObserver){
        val ioScope = CoroutineScope(Dispatchers.IO + Job())
        Firebase.auth.uid?.let{myUserId ->
            dbRepository.loadAndObserveNewMessagesAdded(
                myUserId,
                observer
            ) { result ->
                when (result) {
                    is Result.Error -> {
                        Timber.tag(tag).e("Error loading Message")
                    }

                    Result.Loading -> {}
                    is Result.Progress -> {}
                    is Result.Success -> {
                        result.data?.let {
                            //Update room
                            val message = firebaseMessageToRoomMessage(it)
                            ioScope.launch {
                                //update message table
                                chatMeUpDatabase.messageDao.upsertMessage(message)
                                //update chat LastMessage
                                val chat = chatMeUpDatabase.chatDao.getChat(message.chatID).apply {
                                    no_of_unread_messages += 1
                                    lastMessageText = message.messageText
                                    lastMessageTime = message.messageTime
                                    messageType = enumValueOf(message.messageType)
                                }
                                chatMeUpDatabase.chatDao.upsertChat(chat)

                            }
                            //Delete from Firebase
                            dbRepository.removeNewMessages(myUserId, it.messageID)
                        }
                    }
                }
            }
        }
    }
}
