package com.example.vehiclenumberidenticationapp

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.VehicleNumberIdentificationAppTheme
import com.example.vehiclenumberidenticationapp.models.PoliceUser
import com.example.vehiclenumberidenticationapp.ui.screens.LoginChoiceScreen
import com.example.vehiclenumberidenticationapp.ui.screens.LoginPage
import com.example.vehiclenumberidenticationapp.ui.screens.PoliceUserPage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

sealed class Destination (val route: String) {
    object LoginChoicePage: Destination ( "login_choice")
    object LoginPage: Destination( "login_page/{isAdmin}") {
        fun createRoute (isAdmin: Boolean) = "login_page/$isAdmin"
    }
    object PoliceUserPage: Destination ( "police_user_page/{policeUser}"){
        fun createRoute (policeUser: String) = "police_user_page/$policeUser"
    }

}

class MainActivity : ComponentActivity() {
    private val auth by lazy{
        Firebase.auth
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VehicleNumberIdentificationAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    VehicleNumberIdentificationAppNavigation(navController = navController)
                }
            }
        }
    }
}

@Composable
fun VehicleNumberIdentificationAppNavigation(navController: NavHostController){
    val ctx = LocalContext.current
    NavHost(navController = navController, startDestination = "login_choice"){
        composable(Destination.LoginChoicePage.route) { LoginChoiceScreen(navController) }
        composable(Destination.LoginPage.route){ navBackStackEntry ->
            val isAdmin = navBackStackEntry.arguments?.getString("isAdmin").toBoolean()
            LoginPage(navController = navController, isAdmin = isAdmin, auth = Firebase.auth)
        }
        composable(Destination.PoliceUserPage.route){ navBackStackEntry ->
            val email = navBackStackEntry.arguments?.getString("policeUser")
            if (email != null) {
                PoliceUserPage(policeUser = email)
            }
        }
    }
}