package com.nomba.topwisetest

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.topwise.manager.emv.entity.EmvEntity

enum class CardReadState{
    INSERT_CARD,
    INPUT_PIN,
    OUTPUT
}

@Composable
fun InsertCardScreen(
    activity: MainActivity,
    context: Context
){
    val cardReadState by remember {
        mutableStateOf(CardReadState.INSERT_CARD)
    }

    var pin by remember {
        mutableStateOf("")
    }
    var output by remember {
        mutableStateOf("")
    }

    val emvEntity = EmvEntity()

    Text(
        text = "Card Test",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    when(cardReadState){
        CardReadState.INSERT_CARD -> {
            Text(
                text = "Card Test",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        CardReadState.INPUT_PIN -> {
            PinInputView(pin = pin,
                context = context,
                onPinValueChanged = {
                    pin = if(pin.length >= 4){
                        Toast.makeText(
                            context,
                            "Invalid Pin",
                            Toast.LENGTH_SHORT
                        ).show()
                        ""
                    } else it
                },
                callback = {
                    activity.usdkManage.emvHelper

//                    cardReadState = CardReadState.OUTPUT
                }
            )
        }
        CardReadState.OUTPUT -> {
            Text(
                text = output
            )

        }
    }

}