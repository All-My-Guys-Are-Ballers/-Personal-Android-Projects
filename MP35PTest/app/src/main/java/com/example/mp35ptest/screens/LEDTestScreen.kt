package com.example.mp35ptest

import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import com.topwise.cloudpos.data.LedCode

@Composable
fun LEDTestScreen() {
//    if(DeviceServiceManager() == null) {
//        //make toast
//        return
//    }

    val ledControl = DeviceServiceManager.led
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn() {
            items(ledConfigurationsList) {
                Card(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(0.5f)
//                    .clip(RoundedCornerShape(4.dp)
                    ,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(text = it.toString(),
                        modifier = Modifier
                            .clickable {
                                when (it) {
                                    LEDConfigurations.RED_LED_ON -> {
                                        if (ledControl == null) Log.d(ContentValues.TAG, "Help")
                                        ledControl?.setLed(LedCode.OPER_LED_RED, true)
                                    }
                                    LEDConfigurations.RED_LED_OFF -> ledControl?.setLed(
                                        LedCode.OPER_LED_RED,
                                        false
                                    )
                                    LEDConfigurations.YELLOW_LED_ON -> {
                                        if (ledControl == null) Log.d(ContentValues.TAG, "Help")
                                        ledControl?.setLed(LedCode.OPER_LED_YELLOW, true)
                                    }
                                    LEDConfigurations.YELLOW_LED_OFF -> ledControl?.setLed(
                                        LedCode.OPER_LED_YELLOW,
                                        false
                                    )
                                    LEDConfigurations.GREEN_LED_ON -> {
                                        if (ledControl == null) Log.d(ContentValues.TAG, "Help")
                                        ledControl?.setLed(LedCode.OPER_LED_GREEN, true)
                                    }
                                    LEDConfigurations.GREEN_LED_OFF -> ledControl?.setLed(
                                        LedCode.OPER_LED_GREEN,
                                        false
                                    )
                                    LEDConfigurations.BLUE_LED_ON -> {
                                        if (ledControl == null) Log.d(ContentValues.TAG, "Help")
                                        ledControl?.setLed(LedCode.OPER_LED_BLUE, true)
                                    }
                                    LEDConfigurations.BLUE_LED_OFF -> ledControl?.setLed(
                                        LedCode.OPER_LED_BLUE,
                                        false
                                    )


                                }
                            }
                            .padding(6.dp),
                        textAlign = TextAlign.Center
                    )
                }

            }
        }
    }
}