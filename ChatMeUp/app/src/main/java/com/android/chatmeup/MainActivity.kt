package com.android.chatmeup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.compose.rememberNavController
import com.android.chatmeup.data.datastore.CmuDataStoreRepository
import com.android.chatmeup.ui.CmuApp
import com.android.chatmeup.ui.theme.ChatMeUpTheme
import com.android.chatmeup.ui.theme.cmuDarkBlue
import com.android.chatmeup.ui.theme.cmuOffWhite
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var chatMeUpApp: CmuApplication
    val auth: FirebaseAuth = Firebase.auth
    val database: FirebaseDatabase = Firebase.database

    @Inject
    lateinit var cmuDataStoreRepository: CmuDataStoreRepository

//    @EntryPoint
//    @InstallIn(ActivityComponent::class)
//    interface ViewModelFactoryProvider {
//        fun homeViewModelFactory(): HomeViewModel.Factory
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        chatMeUpApp = applicationContext as CmuApplication

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
//                    color = MaterialTheme.colorScheme.background
                ) {
//                    HomeScreen(context = applicationContext, activity = chatMeUpApp.getCurrentActivity() )
                    CmuApp(
                        context = applicationContext,
                        activity = this,
                        navController = navController,
                        chatMeUpApp = chatMeUpApp,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        chatMeUpApp.setCurrentActivity(this)
    }
    override fun onPause() {
        super.onPause()
        chatMeUpApp.setCurrentActivity(this)
    }
}
