package com.example.githubsearchengine

import android.graphics.Paint.Align
import android.os.Bundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.githubsearchengine.ui.theme.GitHubSearchEngineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GitHubSearchEngineTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SearchBar()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview
@Composable
fun SearchBar(){
    var text by remember { mutableStateOf(TextFieldValue("")) }

    Row(modifier = Modifier.fillMaxWidth()){
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            label = {
                Text(
                    text = stringResource(id = R.string.search)
                )
            },
            singleLine = true,
            maxLines = 1,
            shape = RoundedCornerShape(32.dp)
        )
        IconButton(onClick = { /*TODO*/ }, modifier = Modifier
//            .align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search")
        }
    }
}

fun ListItem(){

}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GitHubSearchEngineTheme {
        Greeting("Android")
    }
}