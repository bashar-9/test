package com.bashar.cinematicphotoeditor.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// A data class to represent a single adjustment tool
data class AdjustmentTool(
    val name: String,
    val icon: ImageVector,
    val initialValue: Float,
    val valueRange: ClosedFloatingPointRange<Float>
)

// A central library of all our available adjustment tools
object AdjustmentTools {
    fun getTools(): List<AdjustmentTool> {
        return listOf(
            AdjustmentTool("Contrast", Icons.Default.Contrast, 1.0f, 0.5f..1.5f),
            AdjustmentTool("Exposure", Icons.Default.Brightness6, 0.0f, -0.5f..0.5f),
            AdjustmentTool("Highlights", Icons.Default.Highlight, 0.0f, -1.0f..1.0f),
            AdjustmentTool("Shadows", Icons.Default.NightsStay, 0.0f, -1.0f..1.0f),
            AdjustmentTool("Saturation", Icons.Default.Colorize, 1.0f, 0.0f..2.0f),
            AdjustmentTool("Temperature", Icons.Default.DeviceThermostat, 0.0f, -1.0f..1.0f),
            AdjustmentTool("Vignette", Icons.Default.FilterBAndW, 0.0f, 0.0f..1.0f),
            AdjustmentTool("Sharpness", Icons.Default.FilterCenterFocus, 0.0f, 0.0f..1.0f)
            // We can add more here later, like Hue, Grain, etc.
        )
    }
}
