package com.android.chatmeup.ui.screens.components

import android.net.Uri
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextImage(
    modifier: Modifier = Modifier,
    imageUri: Uri,
    onClick: () -> Unit
){
    Card(
        shape = RoundedCornerShape(
            topStart = 15.dp,
            topEnd = 15.dp,
            bottomStart = 15.dp,
            bottomEnd = 15.dp
        ),
        onClick = onClick,
        modifier = modifier.wrapContentSize()
    ) {
        AsyncImage(
            modifier = Modifier.heightIn(30.dp, 300.dp),
            model = imageUri,
            contentScale = ContentScale.Crop,
            contentDescription = ""
        )
    }
}