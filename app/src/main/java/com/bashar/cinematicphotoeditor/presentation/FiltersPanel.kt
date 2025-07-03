package com.bashar.cinematicphotoeditor.presentation

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bashar.cinematicphotoeditor.domain.BitmapFilterer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FiltersPanel(
    originalBitmap: Bitmap?,
    presets: List<FilterPreset>,
    selectedPreset: FilterPreset?,
    onPresetClick: (FilterPreset) -> Unit,
    intensity: Float,
    onIntensityChange: (Float) -> Unit,
    onResetIntensity: () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        // This Box creates a fixed-height area for the slider.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selectedPreset != null && selectedPreset.name != "None") {
                AdjustmentSlider(
                    label = "Intensity",
                    value = intensity,
                    valueRange = 0f..100f,
                    onValueChange = onIntensityChange,
                    onReset = onResetIntensity
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { preset ->
                // The main change is calling our new preview item.
                FilterPreviewItem(
                    originalBitmap = originalBitmap,
                    preset = preset,
                    isSelected = preset.name == selectedPreset?.name,
                    onPresetClick = onPresetClick
                )
            }
        }
    }
}


@Composable
private fun FilterPreviewItem(
    originalBitmap: Bitmap?,
    preset: FilterPreset,
    isSelected: Boolean,
    onPresetClick: (FilterPreset) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    // This state will hold our small, filtered thumbnail bitmap.
    var thumbnailBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // This effect runs only when the original image or the preset changes.
    // It creates the thumbnail on a background thread to keep the UI smooth.
    LaunchedEffect(originalBitmap, preset) {
        if (originalBitmap != null) {
            coroutineScope.launch(Dispatchers.Default) {
                thumbnailBitmap = BitmapFilterer.applyColorMatrix(originalBitmap, preset.matrix)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp) // Give the item a fixed width
            .clickable { onPresetClick(preset) }
    ) {
        val borderModifier = if (isSelected) {
            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        } else {
            Modifier
        }

        Box(
            modifier = borderModifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // If the thumbnail has been created, show it.
            if (thumbnailBitmap != null) {
                Image(
                    bitmap = thumbnailBitmap!!.asImageBitmap(),
                    contentDescription = "${preset.name} filter preview",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Otherwise, show a placeholder while it loads.
                Box(modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                )
            }
        }

        Text(
            text = preset.name,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}