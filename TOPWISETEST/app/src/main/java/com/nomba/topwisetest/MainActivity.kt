package com.nomba.topwisetest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nomba.topwisetest.ui.theme.TOPWISETESTTheme
import com.topwise.manager.TopUsdkManage

val testList: MutableList<String> = mutableListOf(
    "buzzer_test",
    "led_test",
    "swipe_card_test",
    "print_test",
    "insert_card_test"
)
enum class LEDConfigurations {
    RED_LED_ON,
    RED_LED_OFF,
    BLUE_LED_ON,
    BLUE_LED_OFF,
    YELLOW_LED_ON,
    YELLOW_LED_OFF,
    GREEN_LED_ON,
    GREEN_LED_OFF
}

val ledConfigurationsList: List<LEDConfigurations> = listOf(
    LEDConfigurations.RED_LED_OFF,
    LEDConfigurations.RED_LED_ON,
    LEDConfigurations.BLUE_LED_ON,
    LEDConfigurations.BLUE_LED_OFF,
    LEDConfigurations.GREEN_LED_ON,
    LEDConfigurations.GREEN_LED_OFF,
    LEDConfigurations.YELLOW_LED_OFF,
    LEDConfigurations.YELLOW_LED_ON
)
sealed class Destination (val route: String) {
    object HomePage: Destination ( "home_page")
    object LEDTestPage: Destination( "led_test")
    object BuzzerTestPage: Destination ("buzzer_test")
    object SwipeCardTestPage: Destination ("swipe_card_test")
    object PrintTestPage: Destination ("print_test")
    object InsertCardTestPage: Destination ("insert_card_test")

    val POS_T1 = 0x01 //0x01 T1  0x02 MP35P

    val POS_MP35P = 0x02
    var POS_MODE = POS_T1

}

class MainActivity : ComponentActivity() {
    val usdkManage: TopUsdkManage = TopUsdkManage.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
//        DeviceServiceManager.bindDeviceService(this)
        super.onCreate(savedInstanceState)
        setContent {
            TOPWISETESTTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    TOPWISETESTApp(navController = navController)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}


@Composable
fun TOPWISETESTApp(navController: NavHostController){
    NavHost(navController = navController, startDestination = "home_page"){
        composable(Destination.HomePage.route) { HomePage(navController) }
        composable(Destination.BuzzerTestPage.route){}
        composable(Destination.LEDTestPage.route){  }
        composable(Destination.SwipeCardTestPage.route){  }
        composable(Destination.PrintTestPage.route){  }
        composable(Destination.InsertCardTestPage.route) {  }
    }
}

@Composable
fun HomePage(navController: NavHostController){
    Surface(modifier = Modifier
        .fillMaxSize()
    ) {
        LazyColumn(){
            items(testList){
                Card(modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(0.5f)
//                    .clip(RoundedCornerShape(4.dp)
                    ,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(text = it,
                        modifier = Modifier
                            .clickable {
                                navController.navigate(it)
                            }
                            .padding(6.dp),
                        textAlign = TextAlign.Center
                    )

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
    TOPWISETESTTheme {
        Greeting("Android")
    }
}