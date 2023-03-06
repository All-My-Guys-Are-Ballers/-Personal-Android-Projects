package com.example.vehiclenumberidenticationapp

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.example.vehiclenumberidenticationapp.ui.screens.RegisterUserScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

sealed class Destination (val route: String) {
    object LoginChoicePage: Destination ( "login_choice")
    object LoginPage: Destination( "login_page")
    object PoliceUserPage: Destination ( "police_user_page/{policeUser}"){
        fun createRoute (policeUser: String) = "police_user_page/$policeUser"
    }
    object RegisterUserScreen: Destination("register_user_screen")
}


//private class YourImageAnalyzer : ImageAnalysis.Analyzer {
//
//    override fun analyze(imageProxy: ImageProxy) {
//        val mediaImage = imageProxy.image
//        if (mediaImage != null) {
//            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//            // Pass image to an ML Kit Vision API
//            // ...
//        }
//    }
//}



class MainActivity : ComponentActivity() {
    private val auth by lazy{
        Firebase.auth
    }

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("kilo", "Permission granted")
            shouldShowCamera.value = true //
        } else {
            Log.i("kilo", "Permission denied")
        }
    }

    private fun handleImageCapture(uri: Uri) {
        Log.i("kilo", "Image captured: $uri")
        shouldShowCamera.value = false
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("kilo", "Permission previously granted")
                shouldShowCamera.value = true // 👈🏽
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> Log.i("kilo", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
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
                    VehicleNumberIdentificationAppNavigation(navController = navController, auth = auth)
                }
            }
        }
        requestCameraPermission()
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
}

@Composable
fun VehicleNumberIdentificationAppNavigation(navController: NavHostController, auth: FirebaseAuth){
    val ctx = LocalContext.current
    NavHost(navController = navController, startDestination = "login_page"){
        composable(Destination.LoginChoicePage.route) { LoginChoiceScreen(navController) }
        composable(Destination.LoginPage.route){
            LoginPage(navController = navController, auth = Firebase.auth)
        }
        composable(Destination.PoliceUserPage.route){ navBackStackEntry ->
            val email = navBackStackEntry.arguments?.getString("policeUser")
            if (email != null) {
                PoliceUserPage(policeUser = email)
            }
        }
        composable(Destination.RegisterUserScreen.route){ RegisterUserScreen(
            navController = navController,
            auth = auth
        )}
    }
}