package com.bashar.cinematicphotoeditor.presentation

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bashar.cinematicphotoeditor.domain.HistogramData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun HistogramView(histogramData: HistogramData?) {
    if (histogramData == null) return // Don't show anything if there's no data

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        factory = { context ->
            LineChart(context).apply {
                // Basic chart setup
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.setDrawLabels(false)
                xAxis.setDrawLabels(false)
                setTouchEnabled(false)
            }
        },
        update = { chart ->
            // This block updates the chart with new data
            val redEntries = histogramData.red.mapIndexed { index, value -> Entry(index.toFloat(), value.toFloat()) }
            val greenEntries = histogramData.green.mapIndexed { index, value -> Entry(index.toFloat(), value.toFloat()) }
            val blueEntries = histogramData.blue.mapIndexed { index, value -> Entry(index.toFloat(), value.toFloat()) }

            val redDataSet = LineDataSet(redEntries, "Red").apply {
                color = Color.RED
                setDrawCircles(false)
                setDrawValues(false)
            }
            val greenDataSet = LineDataSet(greenEntries, "Green").apply {
                color = Color.GREEN
                setDrawCircles(false)
                setDrawValues(false)
            }
            val blueDataSet = LineDataSet(blueEntries, "Blue").apply {
                color = Color.BLUE
                setDrawCircles(false)
                setDrawValues(false)
            }

            chart.data = LineData(redDataSet, greenDataSet, blueDataSet)
            chart.invalidate() // Refresh the chart
        }
    )
}