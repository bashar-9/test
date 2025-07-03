package com.bashar.cinematicphotoeditor.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// Add a new property to track if the tool is functional
data class AdjustmentTool(
    val name: String,
    val icon: ImageVector,
    val initialValue: Float,
    val valueRange: ClosedFloatingPointRange<Float>,
    val isImplemented: Boolean = false // Default to false
)

object AdjustmentTools {
    fun getTools(): List<AdjustmentTool> {
        return listOf(
            // These are working, so we set them to true
            AdjustmentTool("Contrast", Icons.Default.Contrast, 1.0f, 0.5f..1.5f, isImplemented = true),
            AdjustmentTool("Exposure", Icons.Default.Brightness6, 0.0f, -0.5f..0.5f, isImplemented = true),
            AdjustmentTool("Saturation", Icons.Default.Colorize, 1.0f, 0.0f..2.0f, isImplemented = true),
            AdjustmentTool("Temperature", Icons.Default.DeviceThermostat, 0.0f, -1.0f..1.0f, isImplemented = true),

              // These are not implemented yet, so they will use the default 'false'
//            AdjustmentTool("Highlights", Icons.Default.Highlight, 0.0f, -1.0f..1.0f),
//            AdjustmentTool("Shadows", Icons.Default.NightsStay, 0.0f, -1.0f..1.0f),
//            AdjustmentTool("Vignette", Icons.Default.FilterBAndW, 0.0f, 0.0f..1.0f),
//            AdjustmentTool("Sharpness", Icons.Default.FilterCenterFocus, 0.0f, 0.0f..1.0f)
        )
    }
}