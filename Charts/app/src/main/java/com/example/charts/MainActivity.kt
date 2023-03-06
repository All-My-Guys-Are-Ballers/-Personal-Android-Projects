package com.example.charts

import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.F
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.example.charts.renderer.NxYAxisDrawer
import com.example.charts.ui.theme.ChartsTheme
import com.github.tehras.charts.bar.BarChart
import com.github.tehras.charts.bar.BarChartData
import com.github.tehras.charts.bar.renderer.label.SimpleValueDrawer
import com.github.tehras.charts.bar.renderer.yaxis.SimpleYAxisDrawer
import com.github.tehras.charts.bar.renderer.yaxis.YAxisDrawer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChartsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    NxBarChartScreen()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun NxBarChart(modifier: Modifier = Modifier, barChartData: BarChartData){
    val context = LocalContext.current
    Surface(modifier = modifier.fillMaxWidth().padding(20.dp), border = BorderStroke(1.dp, Color.Black)
    ) {
        Box(modifier = Modifier.padding(12.dp)){
            BarChart(barChartData = barChartData,
                labelDrawer = SimpleValueDrawer(
                    drawLocation = SimpleValueDrawer.DrawLocation.Outside
                ),
                yAxisDrawer = NxYAxisDrawer(context)
                )
        }
    }
}

@Composable
fun NxBarChartScreen(modifier: Modifier = Modifier){
    val barChartData = BarChartData(
        listOf(
            BarChartData.Bar(0.7f, Color.Blue,"Joshua"),
            BarChartData.Bar(0.3f, Color.Red,"Victor"),
            BarChartData.Bar(0.1f, Color.LightGray, "Toothbrush")
        ), padBy = 20F, startAtZero = false)
    Scaffold(
        backgroundColor = Color.White,
        modifier = Modifier.fillMaxHeight()
    ) {paddingValues ->
        Column(modifier = modifier.padding(paddingValues)) {
            NxBarChart(modifier = Modifier.height(250.dp), barChartData = barChartData)
            Spacer(Modifier.height(20.dp))
            Text(text = "This is a barchart of stuffs",
                style = MaterialTheme.typography.button,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color.Black,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )

        }
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChartsTheme {
        NxBarChartScreen()
    }
}