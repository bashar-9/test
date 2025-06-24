package com.bashar.cinematicphotoeditor.presentation

// We now import the classic Android ColorMatrix, as it's easier to manipulate for blending.
import android.graphics.ColorMatrix

// The FilterPreset now holds its name and its corresponding ColorMatrix
data class FilterPreset(
    val name: String,
    val matrix: ColorMatrix
)

// A central object to hold our library of predefined filters
object FilterLibrary {
    fun getPresets(): List<FilterPreset> {
        return listOf(
            FilterPreset("None", ColorMatrix()), // An identity matrix that does nothing
            FilterPreset("B&W", ColorMatrix().apply { setSaturation(0f) }),
            FilterPreset("Cinematic", ColorMatrix(floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, -10f, 0.0f, 0.9f, 0.1f, 0.0f, 0f, 0.1f, 0.2f, 0.7f, 0.0f, 10f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f))),
            FilterPreset("Vintage", ColorMatrix(floatArrayOf(0.393f, 0.769f, 0.189f, 0f, 0f, 0.349f, 0.686f, 0.168f, 0f, 0f, 0.272f, 0.534f, 0.131f, 0f, 0f, 0f, 0f, 0f, 1f, 0f))),
            FilterPreset("Vibrant", ColorMatrix().apply { setSaturation(1.5f) }),
            FilterPreset("Muted", ColorMatrix().apply { setSaturation(0.5f) }),
            FilterPreset("Cool", ColorMatrix(floatArrayOf(1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1.2f, 0f, 0f, 0f, 0f, 0f, 1f, 0f))),
            FilterPreset("Warm", ColorMatrix(floatArrayOf(1.1f, 0f, 0f, 0f, 0f, 0f, 1.1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f))),
            FilterPreset("Invert", ColorMatrix(floatArrayOf(-1f, 0f, 0f, 0f, 255f, 0f, -1f, 0f, 0f, 255f, 0f, 0f, -1f, 0f, 255f, 0f, 0f, 0f, 1f, 0f))),
            FilterPreset("Studio", ColorMatrix().apply { val scale = 1.2f; setScale(scale, scale, scale, 1f) })
        )
    }
}
