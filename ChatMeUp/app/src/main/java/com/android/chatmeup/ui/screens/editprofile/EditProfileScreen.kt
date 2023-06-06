package com.android.chatmeup.ui.screens.editprofile

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.chatmeup.ui.screens.components.CmuInputTextField
import com.android.chatmeup.ui.screens.components.ImagePage
import com.android.chatmeup.ui.theme.seed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val MAX_DISPLAY_NAME_LENGTH = 25

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun EditProfileScreen(
    context: Context,
    activity: Activity?,
    onBackClick: () -> Unit
){
    val scope = rememberCoroutineScope()

    val profileImageUri = Uri.parse("")

    var displayName = ""

    var displayNameTextLengthRemaining by remember {
        mutableStateOf(MAX_DISPLAY_NAME_LENGTH - displayName.length)
    }

    var currentBottomSheet: BottomSheetScreen by rememberSaveable{
        mutableStateOf(BottomSheetScreen.ProfileImagePage(profileImageUri))
    }

    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )

    BottomSheetScaffold(sheetContent = {
        SheetLayout(
            scope = scope,
            currentScreen = currentBottomSheet,
            modalBottomSheetState = modalBottomSheetState,
            context = context,
            activity = activity,
            displayName = displayName,
            onValueChanged =
            {
                displayName = it
            },
            onDismissButtonClicked = { /*TODO*/ },
            onConfirm = {},
            displayNameTextLengthRemaining = displayNameTextLengthRemaining
        ) }
    ) {

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditDisplayNameDialog(
    onDismissButtonClicked: () -> Unit,
    onConfirm: (String) -> Unit,
    displayName: String,
    onValueChanged: (String) -> Unit,
    textLengthRemaining: Int
){
    val keyboardController = LocalSoftwareKeyboardController.current
    keyboardController?.show()
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        CmuInputTextField(
            placeholder = "Display Name",
            text = displayName,
            onValueChanged = onValueChanged,
            trailingIcon = {
                Text(
                    text = textLengthRemaining.toString(),
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )


        Box(
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 8.dp, end = 6.dp)
        ) {
            val textStyle =
                MaterialTheme.typography.labelLarge
            ProvideTextStyle(value = textStyle) {
                Row(
                ) {
                    TextButton(
                        onClick = onDismissButtonClicked,
                    ) {
                        Text(
                            "Cancel",
                            color = seed,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    TextButton(
                        onClick = {
                            onConfirm(displayName)
                        },
                    ) {
                        Text(
                            "Save",
                            color = seed,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetLayout(
    scope: CoroutineScope,
    currentScreen: BottomSheetScreen,
    modalBottomSheetState: ModalBottomSheetState,
    context: Context,
    activity: Activity?,
    displayName: String,
    displayNameTextLengthRemaining: Int,
    onValueChanged: (String) -> Unit,
    onDismissButtonClicked: () -> Unit,
    onConfirm: (String) -> Unit
) {
    when(currentScreen){
        BottomSheetScreen.EditDisplayName -> {
            EditDisplayNameDialog(
                onDismissButtonClicked = onDismissButtonClicked ,
                onConfirm = onConfirm,
                displayName = displayName,
                onValueChanged = onValueChanged,
                textLengthRemaining = displayNameTextLengthRemaining
            )
        }
        is BottomSheetScreen.ProfileImagePage -> {
            ImagePage(
                title = "Profile photo",
                imageUri = currentScreen.imageUri,
                onBackClick = {
                    scope.launch {
                        modalBottomSheetState.hide()
                    }
                }
            )
        }

    }
}


sealed class BottomSheetScreen {
    object EditDisplayName: BottomSheetScreen()
    data class ProfileImagePage(val imageUri: Uri): BottomSheetScreen()
}