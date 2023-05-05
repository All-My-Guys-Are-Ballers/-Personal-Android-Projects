package com.android.chatmeup.ui.screens.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.chatmeup.ui.theme.success_green

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePicture(
    @DrawableRes imageId: Int,
    isOnline: Boolean = false,
    size: Dp = 60.dp,
){
    BadgedBox(
        modifier = Modifier
            .size(size),
        badge = {
            if(isOnline){
                Card(
                    modifier = Modifier
                        .size(size / 3)
                        .offset(x = (-(size.value / 6)).dp, y = (size.value / 6).dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Icon(
                        modifier = Modifier
//                        .align(Alignment.TopEnd)
                            .padding((size.value / 30).dp),
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        tint = success_green
                    )
                }
            }
        },
    ){
        Image(
            modifier = Modifier
                .clip(RoundedCornerShape(30))
                .fillMaxSize(),
            contentScale = ContentScale.Crop,
            painter = painterResource(id = imageId),
            contentDescription = "Profile picture"
        )
    }
}