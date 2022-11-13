package com.example.superheroes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.superheroes.data.Hero
import com.example.superheroes.data.heroes
import com.example.superheroes.ui.theme.SuperHeroesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperHeroesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SuperheroApp()
                }
            }
        }
    }
}

@Composable
fun SuperheroItem(
    modifier: Modifier = Modifier,
    hero: Hero = heroes[0]
) {
    Card(modifier = modifier
        .padding(16.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .height(108.dp),
        elevation = 2.dp,

        ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxHeight()
        ) {
            SuperheroInformation(modifier = Modifier.weight(1f), hero.nameRes, hero.descriptionRes)
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = hero.imageRes),
                contentDescription = stringResource(id = hero.descriptionRes),
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .fillMaxHeight(),
//                contentScale = ContentScale.FillWidth
            )
        }
    }
}

@Composable
fun SuperheroApp(){
    Scaffold(topBar = { TopAppBar() }){
        LazyColumn(Modifier.background(MaterialTheme.colors.background)) {
            items(heroes) {
                SuperheroItem(modifier = Modifier, hero = it)
            }
        }
    }
}

@Composable
fun TopAppBar(){
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.h1,
            modifier = Modifier
                .height(56.dp)
                .fillMaxSize()
                .wrapContentSize()

        )
}


@Composable
fun SuperheroInformation(
    modifier: Modifier = Modifier,
    @StringRes superheroName:Int,
    @StringRes superheroDescription:Int
){
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = superheroName),
            style = MaterialTheme.typography.h3,
        )
        Text(
            text = stringResource(id = superheroDescription),
            style = MaterialTheme.typography.body1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SuperHeroesTheme (darkTheme = true){
        SuperheroApp()
    }
}