package com.example.charttest.model

import com.github.tehras.charts.piechart.PieChartData

data class PieChartDataModel(
    var slices: List<PieChartData.Slice>,
    val sliceThickness: Float
)
