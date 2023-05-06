package com.android.chatmeup.ui.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.chatmeup.ui.theme.neutral_disabled

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CmuInputTextField(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String,
    paddingValues: PaddingValues = PaddingValues(top = 16.dp, start = 30.dp, end = 30.dp),
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    text: MutableState<TextFieldValue>,
    imeAction: ImeAction = ImeAction.Done,
    onValueChanged: (TextFieldValue) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    onDone: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(12.dp)
){
    val keyboardState = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues),
    ) {
        if(label.isNotBlank()){
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
            ,
            value = text.value,
            onValueChange = {
                onValueChanged(it)
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
//            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = {
                Text(
                    text = placeholder,
                    color = neutral_disabled,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            shape = shape,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            leadingIcon = leadingIcon,
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardState?.hide()
                    onDone()
                         },
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                selectionColors = TextSelectionColors(
                    backgroundColor = neutral_disabled,
                    handleColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        )
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CmuInputTextField(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String,
    paddingValues: PaddingValues = PaddingValues(top = 16.dp, start = 30.dp, end = 30.dp),
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    text: String,
    imeAction: ImeAction = ImeAction.Done,
    onValueChanged: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    onDone: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(12.dp)
){
    val keyboardState = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues),
    ) {
        if(label.isNotBlank()){
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
            ,
            value = text,
            onValueChange = {
                onValueChanged(it)
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
//            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = {
                Text(
                    text = placeholder,
                    color = neutral_disabled,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            shape = shape,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            leadingIcon = leadingIcon,
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardState?.hide()
                    onDone()
                },
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                selectionColors = TextSelectionColors(
                    backgroundColor = neutral_disabled,
                    handleColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        )
    }
}