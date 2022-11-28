package com.example.githubsearch

import android.content.ContentValues.TAG
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.githubsearch.models.GithubRepo
import com.example.githubsearch.models.Owner
import com.example.githubsearch.ui.screens.GitHubUiState
import com.example.githubsearch.ui.screens.GitHubViewModel
import com.example.githubsearch.ui.theme.GitHubSearchTheme
import timber.log.Timber

val exampleGithubRepo: GithubRepo = GithubRepo(
    id = 76,
    name = "Joshua",
    fullName = "Joshua Owolabi",
    owner = Owner(
        login = "VIPlearner",
        avatarUrl = "https://duet-cdn.vox-cdn.com/thumbor/0x0:2370x1574/1200x800/filters:focal(1185x787:1186x788):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/20103707/Screen_Shot_2020_07_21_at_9.38.25_AM.png"
    ),
    description = "Vibes on vibes",
    stargazersCount = 56,
    watchersCount = 76,
    forksCount = 79
)

var searchText = "Wordle"
var searchPage = 1



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())
        super.onCreate(savedInstanceState)
        setContent {
            GitHubSearchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val githubViewModel: GitHubViewModel = viewModel()
                    githubViewModel.getGitHubRepos(page = 1, query = "Wordle")
                    GitHubSearchScreen(gitHubViewModel = githubViewModel)

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(gitHubViewModel: GitHubViewModel, searchText: String, onValueChange: (String) -> Unit){

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current


    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)){
        TextField(
            value = searchText,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            label = {
                Text(
                    text = stringResource(id = R.string.search)
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                searchPage = 1
                gitHubViewModel.getGitHubRepos(searchPage, searchText)
                focusManager.clearFocus()
            }),

            singleLine = true,
            shape = RoundedCornerShape(32.dp)
        )
        IconButton(onClick = {
            searchPage = 1
            gitHubViewModel.getGitHubRepos(searchPage , searchText)
            keyboardController?.hide()
                             },
            modifier = Modifier

        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search")
        }
    }
}

@Composable
fun GitHubSearchScreen(gitHubViewModel: GitHubViewModel ){
    var searchText by remember {
        mutableStateOf("Jetpack Compose")
    }
    Scaffold(topBar = { TopAppBar() }){
        Column {
            SearchBar(gitHubViewModel, searchText) { searchText = it }
            when (gitHubViewModel.githubUiState) {
                is GitHubUiState.Success -> (gitHubViewModel.githubUiState as GitHubUiState.Success).response?.items?.let { it1 -> GithubRepoList(repoList = it1) }
                is GitHubUiState.Loading -> LoadingScreen()
                is GitHubUiState.Error -> ErrorScreen()
            }
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
            .fillMaxHeight(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween){
            TextButton(
                onClick = { if (searchPage > 1){
                    searchPage-- }
                          gitHubViewModel.getGitHubRepos(searchPage, searchText)},
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
            ) {
                Text(
                    text = stringResource(id = R.string.previous),
                    color = MaterialTheme.colors.onSecondary
                )
            }
            TextButton(
                onClick = { searchPage++
                          gitHubViewModel.getGitHubRepos(searchPage, searchText)},
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)

            ) {
                Text(
                    text = stringResource(id = R.string.next),
                    color = MaterialTheme.colors.onSecondary
                )
            }
        }
    }
}

@Composable
fun GithubRepoList(repoList: List<GithubRepo>){
    LazyColumn {
        items(repoList) {
            GitHubRepoItem(githubRepo = it)
        }
    }
}

@Composable
fun TopAppBar(){
    Text(
        text = "GitHub Search",
        modifier = Modifier
            .wrapContentSize(align = Alignment.Center)
            .fillMaxWidth(),
        style = MaterialTheme.typography.h1,
        textAlign = TextAlign.Center
    )
}

@Preview
@Composable
fun GitHubRepoItem(modifier: Modifier = Modifier, githubRepo: GithubRepo = exampleGithubRepo){
    var isItemExpanded by remember {
        mutableStateOf(false)
    }
    Card(modifier = modifier
        .fillMaxWidth()
        .padding(8.dp), shape = RoundedCornerShape(12.dp), elevation = 4.dp) {
        Row{
            Column(modifier = Modifier
                .padding(8.dp)
                .weight(1f)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow)
                )
            ) {
                Text(
                    text = githubRepo.name
                )
                Row {
                    AsyncImage(
                        model = githubRepo.owner.avatarUrl,
                        contentDescription = "Avatar of ${githubRepo.name}",
                        modifier = Modifier.size(32.dp)
                    )
                    if (!githubRepo.fullName.isNullOrBlank()) {
                        Text(
                            text = githubRepo.fullName
                        )
                    }
                }
                if (!githubRepo.description.isNullOrBlank()) {
                    Text(
                        text = githubRepo.description
                    )
                }

                if(isItemExpanded){
                    RepoStats(githubRepo = githubRepo)
                }
            }
            IconButton(onClick = { isItemExpanded = !isItemExpanded }) {
                Icon(imageVector = if(isItemExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
            }
        }
    }
}

@Composable
fun RepoStats(githubRepo: GithubRepo){
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Row() {
            Icon(imageVector = Icons.Filled.Star, contentDescription = "${githubRepo.stargazersCount} Stars")
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "${githubRepo.stargazersCount}")
        }
        Row() {
            Icon(imageVector = Icons.Filled.Star, contentDescription = "${githubRepo.forksCount} Stars")
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "${githubRepo.forksCount}")
        }
        Row() {
            Icon(imageVector = Icons.Filled.RemoveRedEye, contentDescription = "${githubRepo.watchersCount} Stars")
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${githubRepo.watchersCount}",
                textAlign = TextAlign.Center
            )

        }
    }
}

@Composable
fun RepoStat(icon: ImageVector, number: Int){

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