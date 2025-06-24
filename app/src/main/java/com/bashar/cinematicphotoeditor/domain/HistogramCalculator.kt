package com.bashar.cinematicphotoeditor.domain

import android.graphics.Bitmap
import android.graphics.Color

data class HistogramData(
    val red: IntArray,
    val green: IntArray,
    val blue: IntArray
)

object HistogramCalculator {
    fun calculate(bitmap: Bitmap): HistogramData {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val redChannel = IntArray(256)
        val greenChannel = IntArray(256)
        val blueChannel = IntArray(256)

        for (pixel in pixels) {
            redChannel[Color.red(pixel)]++
            greenChannel[Color.green(pixel)]++
            blueChannel[Color.blue(pixel)]++
        }
        return HistogramData(red = redChannel, green = greenChannel, blue = blueChannel)
    }
}