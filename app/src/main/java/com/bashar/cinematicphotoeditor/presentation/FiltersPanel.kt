package com.bashar.cinematicphotoeditor.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FiltersPanel(
    presets: List<FilterPreset>,
    selectedPreset: FilterPreset?,
    onPresetClick: (FilterPreset) -> Unit,
    intensity: Float,
    onIntensityChange: (Float) -> Unit,
    onResetIntensity: () -> Unit
) {
    Column(
        modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
    ) {
        // Show the intensity slider ONLY if a filter (other than "None") is selected
        if (selectedPreset != null && selectedPreset.name != "None") {
            AdjustmentSlider(
                label = "Intensity",
                value = intensity,
                valueRange = 0f..100f,
                onValueChange = onIntensityChange,
                onReset = onResetIntensity // Pass the reset action to the slider
            )
        } else {
            // Add a spacer to maintain a consistent height when the slider is hidden
            Spacer(modifier = Modifier.height(68.dp))
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(presets) { preset ->
                val modifier = if (preset.name == selectedPreset?.name) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                }
                Box(modifier = modifier.padding(2.dp)) {
                    PresetItem(
                        preset = preset,
                        onPresetClick = onPresetClick
                    )
                }
            }
        }
    }
}
