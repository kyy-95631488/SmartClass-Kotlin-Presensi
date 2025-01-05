package com.callcenter.smartclass.ui.home.mainfeaturesgrid.predict

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry

@Composable
fun PredictionChart(predictions: FloatArray) {
    val notStuntedPercentage = predictions[0]
    val stuntedPercentage = predictions[1]

    val chartEntries = listOf(
        FloatEntry(0f, notStuntedPercentage.toFloat()),
        FloatEntry(1f, stuntedPercentage.toFloat())
    )

    val labels = listOf("Tidak Stunting", "Stunting")

    val chartEntryModelProducer = ChartEntryModelProducer(chartEntries)

    val lineChart = lineChart()

    val bottomAxis = bottomAxis(
        valueFormatter = AxisValueFormatter { value, _ ->
            labels.getOrNull(value.toInt()) ?: value.toString()
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Chart(
            chart = lineChart,
            chartModelProducer = chartEntryModelProducer,
            modifier = Modifier.fillMaxSize(),
            startAxis = null,
            bottomAxis = bottomAxis
        )
    }
}
