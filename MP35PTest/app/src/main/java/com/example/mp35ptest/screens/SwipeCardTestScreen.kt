package com.example.mp35ptest.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.example.mp35ptest.DeviceServiceManager
import com.example.mp35ptest.NxMagCardListener
import kotlinx.coroutines.flow.MutableStateFlow

enum class SwipeCardStatus{
    IDLE,
    SEARCHING,
    TIMEOUT,
    SUCCESS,
    ERROR,
    CANCELLED,
    FAILED
}

//val status: SwipeCardStatus? = SwipeCardStatus.IDLE


@Composable
fun SwipeCardTestScreen(){
    val ctx = LocalContext.current
    val magCardDev = DeviceServiceManager.magCardReader
    val globalVarState = NxMagCardListener.globalVar.collectAsState(0) // Collect the Flow as state

    LaunchedEffect(globalVarState.value){
       when(NxMagCardListener.globalVar.value){
           SwipeCardStatus.TIMEOUT -> {
               Toast.makeText(ctx, " Swipe Card Timeout", Toast.LENGTH_SHORT).show()
               NxMagCardListener.globalVar.value = SwipeCardStatus.IDLE
           }

           SwipeCardStatus.CANCELLED -> {
               Toast.makeText(ctx, " Swipe Card Cancelled", Toast.LENGTH_SHORT).show()
               NxMagCardListener.globalVar.value = SwipeCardStatus.IDLE
           }

           SwipeCardStatus.FAILED -> {
               Toast.makeText(ctx, " Swipe Card Failed", Toast.LENGTH_SHORT).show()
               NxMagCardListener.globalVar.value = SwipeCardStatus.IDLE
           }

           SwipeCardStatus.SUCCESS -> {
               Toast.makeText(ctx, " Swipe Card Success", Toast.LENGTH_SHORT).show()
               NxMagCardListener.globalVar.value = SwipeCardStatus.IDLE
           }

           SwipeCardStatus.ERROR -> {
               Toast.makeText(ctx, " Swipe Card Error", Toast.LENGTH_SHORT).show()
               NxMagCardListener.globalVar.value = SwipeCardStatus.IDLE
           }


           else -> {}
       }
   }
//    val isSwipeCard = false
    Surface (modifier = Modifier.fillMaxSize()){
        Column() {
            Button(onClick = {
                if(globalVarState.value != SwipeCardStatus.SEARCHING){
                    NxMagCardListener.globalVar.value = SwipeCardStatus.SEARCHING
                    Toast.makeText(ctx, "Searching", Toast.LENGTH_SHORT).show()
                    magCardDev?.searchCard(6000, NxMagCardListener)
                }
            }) {
                Text(text = "Get Track Data")
            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(onClick = {
                if(NxMagCardListener.globalVar.value == SwipeCardStatus.SEARCHING){
                    magCardDev?.stopSearch()
                    NxMagCardListener.globalVar.value = SwipeCardStatus.CANCELLED
                }
                else {
                    Toast.makeText(ctx, "Swipe Card is not ON", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(text = "Cancel Swipe")
            }

        }

    }
}