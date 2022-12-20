package com.example.vehiclenumberidenticationapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.vehiclenumberidenticationapp.Destination

@Composable
fun LoginChoiceScreen(navController: NavHostController){
    Scaffold(){
        Column(modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()) {
            TextButton(onClick = { navController.navigate(Destination.LoginPage.route) }
            ) {
                Text(text = "Login as an Admin")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate(Destination.LoginPage.route) }) {
                Text(text = "Login as a User")
            }
        }
    }
}
