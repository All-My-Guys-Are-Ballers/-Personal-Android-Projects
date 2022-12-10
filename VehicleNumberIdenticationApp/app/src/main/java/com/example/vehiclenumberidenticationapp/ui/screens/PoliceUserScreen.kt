package com.example.vehiclenumberidenticationapp.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.vehiclenumberidenticationapp.R
import com.example.vehiclenumberidenticationapp.models.PoliceUser

@Composable
fun PoliceUserPage(policeUser: String){
    Text(text = stringResource(id = R.string.loginas,policeUser),
        modifier = Modifier.fillMaxSize().wrapContentSize()
    )
}