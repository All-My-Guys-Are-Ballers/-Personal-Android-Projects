package com.example.mp35ptest.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mp35ptest.testList

@Composable
fun HomePage(navController: NavHostController){
    Surface(modifier = Modifier
        .fillMaxSize()
    ) {
        LazyColumn(){
            items(testList){
                Card(modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(0.5f)
//                    .clip(RoundedCornerShape(4.dp)
                    ,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(text = it,
                        modifier = Modifier
                            .clickable {
                                navController.navigate(it)
                            }
                            .padding(6.dp),
                        textAlign = TextAlign.Center
                    )

                }
            }

        }

    }
}