package com.example.githubsearch

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.githubsearch.models.GithubRepo
import com.example.githubsearch.ui.screens.GitHubUiState
import com.example.githubsearch.ui.screens.GitHubViewModel
import com.example.githubsearch.ui.theme.GitHubSearchTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
        StrictMode.enableDefaults()
        super.onCreate(savedInstanceState)
        setContent {
            GitHubSearchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val githubViewModel: GitHubViewModel = GitHubViewModel(page = 1, query = "VIPlearner")
                    when (githubViewModel.githubUiState) {
                        is GitHubUiState.Success -> (githubViewModel.githubUiState as GitHubUiState.Success).response.body()?.items?.let { GitHubSearchScreen(repoList = it) }
                        is GitHubUiState.Loading -> LoadingScreen()
                        is GitHubUiState.Error -> ErrorScreen()
                    }
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

@Composable
fun GitHubSearchScreen(repoList: List<GithubRepo>){
    Scaffold(topBar = { Text(text = "GitHubSearch")}){
        if (repoList == null) {
            Text(text = "Error")
        } else {
            GitHubRepoItem(githubRepo = repoList[0])
        }
    }
}

//@Preview
@Composable
fun GitHubRepoItem(githubRepo: GithubRepo){
    Card {
        Column(){
            Text(
                text = githubRepo.name
            )
            Row {
//                Image(
//                    painter = ,
//                    contentDescription =
//                )

                Text(text = githubRepo.fullName)
            }

            if (githubRepo.description != null){
                Text(
                    text = githubRepo.description
                )
            }

        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(R.drawable.loading_img),
            contentDescription = stringResource(R.string.loading)
        )
    }
}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(stringResource(R.string.loading_failed))
    }
}




@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GitHubSearchTheme {
        Greeting("Android")
    }
}