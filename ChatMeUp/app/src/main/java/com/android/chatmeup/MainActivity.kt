package com.android.chatmeup

import android.Manifest
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
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.compose.rememberNavController
import com.android.chatmeup.data.datastore.CmuDataStoreRepository
import com.android.chatmeup.data.db.repository.DatabaseRepository
import com.android.chatmeup.ui.CmuApp
import com.android.chatmeup.ui.screens.chat.viewmodel.ChatViewModel
import com.android.chatmeup.ui.screens.homescreen.HomeViewModel
import com.android.chatmeup.ui.theme.ChatMeUpTheme
import com.android.chatmeup.ui.theme.cmuDarkBlue
import com.android.chatmeup.ui.theme.cmuOffWhite
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val permissionRequestCode = 123
    private lateinit var chatMeUpApp: CmuApplication
    private val dbRepository = DatabaseRepository()

    @Inject
    lateinit var cmuDataStoreRepository: CmuDataStoreRepository

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface ViewModelFactoryProvider {
        fun homeViewModelFactory(): HomeViewModel.Factory
        fun chatViewModelFactory(): ChatViewModel.Factory

    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            permissionRequestCode
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        chatMeUpApp = applicationContext as CmuApplication

        dbRepository.updateOnlineStatus(chatMeUpApp.myUserID, true)

        setContent {
            val systemUiController = rememberSystemUiController()
            //change status bar color anytime we change light mode or dark mode
            val isDarkTheme = isSystemInDarkTheme()
            LaunchedEffect(isSystemInDarkTheme()) {
                systemUiController.setStatusBarColor(
                    color = if(isDarkTheme) cmuDarkBlue else cmuOffWhite,
                    darkIcons = false
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
                        chatMeUpApp = chatMeUpApp,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        chatMeUpApp.setCurrentActivity(this)
        dbRepository.updateOnlineStatus(chatMeUpApp.myUserID, true)
    }
    override fun onPause() {
        super.onPause()
        chatMeUpApp.setCurrentActivity(this)
        dbRepository.updateOnlineStatus(chatMeUpApp.myUserID, false)
    }
}
