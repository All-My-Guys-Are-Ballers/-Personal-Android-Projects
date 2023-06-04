package com.chatmeup.features.edit_profile

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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.TextButton
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.android.chatmeup.ui.screens.components.CmuInputTextField
import com.android.chatmeup.ui.screens.components.ImagePage
import com.android.chatmeup.ui.theme.seed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

    var currentBottomSheet: BottomSheetScreen by rememberSaveable{
        mutableStateOf(BottomSheetScreen.ProfileImagePage(profileImageUri))
    }

    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )

    BottomSheetScaffold(sheetContent =
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
            onConfirm = ,
        )
    ) {

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditDisplayNameDialog(
    onDismissButtonClicked: () -> Unit,
    onConfirm: (String) -> Unit,
    context: Context,
    activity: Activity?,
    displayName: String,
    onValueChanged: (String) -> Unit
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
            onValueChanged =
        )


        Box(
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 8.dp, end = 6.dp)
        ) {
            val textStyle =
                MaterialTheme.typography.button
            ProvideTextStyle(value = textStyle) {
                Row(
                ) {
                    TextButton(
                        onClick = onDismissButtonClicked,
                    ) {
                        Text(
                            "Cancel",
                            color = seed,
                            style = MaterialTheme.typography.button
                        )
                    }
                    TextButton(
                        onClick = {

                        },
                    ) {
                        Text(
                            "Save",
                            color = seed,
                            style = MaterialTheme.typography.button
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
    onValueChanged: (String) -> Unit,
    onDismissButtonClicked: () -> Unit,
    onConfirm: (String) -> Unit
) {
    when(currentScreen){
        BottomSheetScreen.EditDisplayName -> {
            EditDisplayNameDialog(
                context = context,
                activity = activity,
                onDismissButtonClicked = onDismissButtonClicked ,
                onConfirm = onConfirm,
                displayName = displayName,
                onValueChanged = onValueChanged
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