package com.android.chatmeup.ui.screens.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.chatmeup.ui.theme.cmuOffWhite

@Composable
fun CmuDarkButton(
    label: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
){
    Button(modifier = Modifier
        .padding(start = 80.dp, end = 80.dp)
        .height(50.dp)
        .fillMaxWidth(),
        onClick = {
            onClick()
                  },
        elevation =  ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 5.dp,
            disabledElevation = 0.dp),
//                ), colors = ButtonDefaults.buttonColors(backgroundColor = cmuBlue),
        shape = RoundedCornerShape(10.dp)
    ) {
        if (isLoading){
            CircularProgressIndicator(
                color = cmuOffWhite,
                modifier = Modifier.align(Alignment.CenterVertically)
                    .size(25.dp)
                ,
                strokeWidth = 2.dp
            )
        }
        else{
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
//                        color = cmuOffWhite
            )
        }
    }
}