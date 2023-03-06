package com.example.vehiclenumberidenticationapp.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.vehiclenumberidenticationapp.R

@Composable
fun PoliceUserPage(policeUser: String){
    Scaffold(){
        Text(
            text = stringResource(id = R.string.login_as, policeUser),
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
        )
    }
}

@Composable
fun PlateNumberInformation(){

}

