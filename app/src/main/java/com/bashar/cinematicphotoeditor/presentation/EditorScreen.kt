package com.bashar.cinematicphotoeditor.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bashar.cinematicphotoeditor.domain.BitmapFilterer
import com.bashar.cinematicphotoeditor.domain.HistogramCalculator
import com.bashar.cinematicphotoeditor.domain.HistogramData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.compose.ui.graphics.ColorMatrix as ComposeColorMatrix

@Composable
fun EditorScreen(encodedImageUri: String) {
    val imageUri = Uri.parse(URLDecoder.decode(encodedImageUri, StandardCharsets.UTF_8.name()))
    val context = LocalContext.current

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var histogramData by remember { mutableStateOf<HistogramData?>(null) }

    var contrastValue by remember { mutableStateOf(1.0f) }
    var exposureValue by remember { mutableStateOf(0.0f) }
    var activePresetMatrix by remember { mutableStateOf<ColorMatrix?>(null) }

    val finalAndroidMatrix = remember(activePresetMatrix, contrastValue, exposureValue) {
        val matrix = activePresetMatrix?.let { ColorMatrix(it) } ?: ColorMatrix()
        val contrastMatrix = ColorMatrix().apply { setScale(contrastValue, contrastValue, contrastValue, 1f) }
        matrix.postConcat(contrastMatrix)
        val exposureMatrix = ColorMatrix().apply {
            val brightness = exposureValue * 255
            set(floatArrayOf(1f, 0f, 0f, 0f, brightness, 0f, 1f, 0f, 0f, brightness, 0f, 0f, 1f, 0f, brightness, 0f, 0f, 0f, 1f, 0f))
        }
        matrix.postConcat(exposureMatrix)
        matrix
    }

    val finalColorFilter = remember(finalAndroidMatrix) { ColorFilter.colorMatrix(ComposeColorMatrix(finalAndroidMatrix.array)) }

    LaunchedEffect(finalAndroidMatrix) {
        // Real-time update with no delay, as requested
        originalBitmap?.let {
            withContext(Dispatchers.Default) {
                val filteredBitmap = BitmapFilterer.applyColorMatrix(it, finalAndroidMatrix)
                val newHistogramData = HistogramCalculator.calculate(filteredBitmap)
                withContext(Dispatchers.Main) {
                    histogramData = newHistogramData
                }
            }
        }
    }

    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            val bitmap = loadBitmapFromUri(imageUri, context)
            if(bitmap != null) {
                originalBitmap = bitmap
                val initialHistogram = HistogramCalculator.calculate(bitmap)
                withContext(Dispatchers.Main) {
                    histogramData = initialHistogram
                }
            }
        }
    }

    val blackAndWhiteMatrix = ColorMatrix().apply { setSaturation(0f) }
    val vintageMatrix = ColorMatrix(floatArrayOf(0.393f, 0.769f, 0.189f, 0f, 0f, 0.349f, 0.686f, 0.168f, 0f, 0f, 0.272f, 0.534f, 0.131f, 0f, 0f, 0f, 0f, 0f, 1f, 0f))
    val cinematicMatrix = ColorMatrix(floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, -10f, 0.0f, 0.9f, 0.1f, 0.0f, 0f, 0.1f, 0.2f, 0.7f, 0.0f, 10f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f))
    val studioMatrix = ColorMatrix().apply { val scale = 1.2f; setScale(scale, scale, scale, 1f) }

    val presets = listOf(FilterPreset("None"), FilterPreset("B&W"), FilterPreset("Vintage"), FilterPreset("Cinematic"), FilterPreset("Studio"))

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Image Preview",
                modifier = Modifier.fillMaxSize(),
                colorFilter = finalColorFilter,
                // --- THE FIX IS HERE ---
                // Using ContentScale.Crop to ensure it fills the screen
                contentScale = ContentScale.Crop
            )
        }
        HistogramView(histogramData = histogramData)
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            AdjustmentSlider(label = "Contrast", value = contrastValue, valueRange = 0.5f..1.5f, onValueChange = { contrastValue = it })
            AdjustmentSlider(label = "Exposure", value = exposureValue, valueRange = -0.5f..0.5f, onValueChange = { exposureValue = it })
            Button(onClick = { contrastValue = 1.0f; exposureValue = 0.0f }, modifier = Modifier.padding(top = 4.dp).align(Alignment.CenterHorizontally)) {
                Text("Reset Adjustments")
            }
        }
        LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center) {
            items(presets) { preset ->
                PresetItem(preset = preset, onPresetClick = { selectedPreset ->
                    activePresetMatrix = when (selectedPreset.name) {
                        "B&W" -> blackAndWhiteMatrix
                        "Vintage" -> vintageMatrix
                        "Cinematic" -> cinematicMatrix
                        "Studio" -> studioMatrix
                        else -> null
                    }
                })
            }
        }
    }
}

private fun loadBitmapFromUri(uri: Uri, context: Context): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}