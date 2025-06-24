package com.bashar.cinematicphotoeditor.presentation

import android.graphics.Color
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bashar.cinematicphotoeditor.domain.HistogramData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun HistogramView(
    histogramData: HistogramData?,
    modifier: Modifier = Modifier // The modifier parameter is now correctly added
) {
    if (histogramData == null) {
        // You can return a placeholder or an empty composable if there's no data yet
        return
    }

    AndroidView(
        // The passed-in modifier is now used here
        modifier = modifier,
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
