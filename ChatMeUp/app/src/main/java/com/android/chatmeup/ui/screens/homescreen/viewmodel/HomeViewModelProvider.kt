/*
 * Copyright (c) 2022. Nomba Financial Services
 *
 * author: Victor Shoaga
 * email: victor.shoaga@nomba.com
 * github: @inventvictor
 *
 */

package com.android.chatmeup.ui.screens.homescreen.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.chatmeup.ui.screens.homescreen.viewmodel.HomeViewModel

@Composable
fun homeViewModelProvider(
    myUserId: String,
    factory: HomeViewModel.Factory,
): HomeViewModel {
    return viewModel(factory = HomeViewModel.provideFactory(
        factory,
        myUserId = myUserId
    ))
}