package com.android.chatmeup.ui.screens.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun CmuTopBar(modifier: Modifier = Modifier, title: String = "Chats", vararg icons: @Composable () -> Unit = emptyArray()){
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(top = 15.dp, start = 30.dp, end = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        for(icon in icons){
            icon()
        }
    }
}