package com.nomba.topwisetest

import android.app.Activity
import android.content.Context
import android.widget.Toast

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinInputView(
    pin: String,
    modifier: Modifier = Modifier,
    context: Context,
//    pinBuilder: StringBuilder,
//    pinBuilderCount: MutableState<Int>,
    onPinValueChanged: (String) -> Unit,
    callback: (String) -> Unit
){
//    var pin by remember {
//        mutableStateOf("")
//    }
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Enter your PIN",
            style = MaterialTheme.typography.h6,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.padding(vertical = 8.dp))

        TextField(
            modifier = Modifier.fillMaxWidth()
                .padding(20.dp)
                .clip(RoundedCornerShape(10.dp))
            ,
            value = pin,
            onValueChange = onPinValueChanged,
            visualTransformation = PasswordVisualTransformation('\u25CF')
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                modifier = Modifier
                    .height(50.dp)
                    .width(132.dp),
                onClick = {
//                    pin = ""
                }, elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 5.dp,
                    disabledElevation = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                shape = RoundedCornerShape(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Clear,
                    tint = Color.Red,
                    contentDescription = null
                )
            }

            Button(
                modifier = Modifier
                    .height(50.dp)
                    .width(132.dp),
                onClick = {
                    if (pin.length < 4) {
                        Toast.makeText(
                            context,
                            "Please input a valid Pin",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        callback(pin)
                    }
                }, elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 5.dp,
                    disabledElevation = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black),
                shape = RoundedCornerShape(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    tint = Color.White,
                    contentDescription = null
                )
            }
        }
    }
}
