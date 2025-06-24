// In domain/BitmapFilterer.kt
package com.bashar.cinematicphotoeditor.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.ColorMatrix

object BitmapFilterer {
    fun applyColorMatrix(sourceBitmap: Bitmap, colorMatrix: ColorMatrix): Bitmap {
        val newBitmap = Bitmap.createBitmap(
            sourceBitmap.width,
            sourceBitmap.height,
            // --- THIS IS THE FIX ---
            // We provide a default config in case the source's is null
            sourceBitmap.config ?: Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(newBitmap)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(sourceBitmap, 0f, 0f, paint)
        return newBitmap
    }
}