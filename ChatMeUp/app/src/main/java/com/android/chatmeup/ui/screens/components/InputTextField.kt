package com.android.chatmeup.ui.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.chatmeup.ui.theme.cmuBlack
import com.android.chatmeup.ui.theme.cmuDarkGrey
import com.android.chatmeup.ui.theme.cmuWhite

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CmuInputTextField(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    text: MutableState<TextFieldValue>,
    imeAction: ImeAction = ImeAction.Done,
    onValueChanged: (TextFieldValue) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
){
    val keyboardState = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 25.dp, end = 25.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.button,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.padding(vertical = 4.dp))

        val customTextSelectionColors = TextSelectionColors(
            handleColor = cmuDarkGrey, backgroundColor = cmuDarkGrey.copy(alpha = 0.4f)
        )

        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                ,
                value = text.value,
                onValueChange = {
                    onValueChanged(it)
                },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                textStyle = MaterialTheme.typography.body1.copy(color = cmuBlack),
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Color.Gray,
                        style = MaterialTheme.typography.button,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = cmuWhite,
                    cursorColor = cmuBlack,
//                    disabledLabelColor = cmuGray,
                    focusedIndicatorColor = cmuBlack,
                    unfocusedIndicatorColor = cmuBlack
                ),
                visualTransformation = visualTransformation,
                trailingIcon = trailingIcon,
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                leadingIcon = leadingIcon,
                keyboardActions = KeyboardActions(
                    onDone = {
                    keyboardState?.hide() },
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                )
            )
        }
    }
}