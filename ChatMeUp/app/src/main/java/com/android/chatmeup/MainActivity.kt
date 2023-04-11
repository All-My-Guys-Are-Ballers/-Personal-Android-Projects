package com.android.chatmeup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.android.chatmeup.ui.screens.login.InputPhoneNoDialog
import com.android.chatmeup.ui.screens.login.InputPhoneNoEventState
import com.android.chatmeup.ui.screens.login.LoginPage
import com.android.chatmeup.ui.theme.ChatMeUpTheme
import com.android.chatmeup.ui.theme.cmuBlack
import com.android.chatmeup.ui.theme.cmuWhite
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            val systemUiController = rememberSystemUiController()
            //change status bar color anytime we change light mode or dark mode
            val isDarkTheme = isSystemInDarkTheme()
            LaunchedEffect(isSystemInDarkTheme()) {
                systemUiController.setStatusBarColor(
                    color = if(isDarkTheme) cmuBlack else cmuWhite,
                    darkIcons = false
                )
            }
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
                val bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                view.updatePadding(bottom = bottom)
                insets
            }
            ChatMeUpTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    LoginPage()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChatMeUpTheme {
        Greeting("Android")
    }
}