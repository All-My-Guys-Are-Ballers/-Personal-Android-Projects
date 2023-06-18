package com.android.chatmeup.ui.screens.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.chatmeup.R
import com.android.chatmeup.ui.theme.seed

//@Preview
@Composable
fun UploadImageScreen(
    imageUri: Uri,
    onUploadCancelled: () -> Unit = {},
    messageText: String = "",
    onValueChanged: (String) -> Unit = {},
    onSendMessage: () -> Unit = {}
){
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ){
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
            ) {
                IconButton(
                    onClick = onUploadCancelled,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.3f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel Upload")
                }
            }
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )

            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                Row(
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CmuInputTextFieldWithLabel(
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .weight(1f)
                            .height(40.dp),
                        label = "",
                        placeholder = "Message",
                        paddingValues = PaddingValues(),
                        singleLine = false,
                        maxLines = 3,
                        text = messageText,
                        onValueChanged = onValueChanged,
                        shape = RoundedCornerShape(10),
                    )

                    IconButton(onClick = onSendMessage) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_send_button),
                            contentDescription = "Send Message button",
                            tint = seed
                        )
                    }
                }
            }
        }
    }
}