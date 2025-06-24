package com.bashar.cinematicphotoeditor.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bashar.cinematicphotoeditor.domain.HistogramCalculator
import com.bashar.cinematicphotoeditor.domain.HistogramData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.compose.ui.graphics.ColorMatrix as ComposeColorMatrix
import android.graphics.ColorMatrix as AndroidColorMatrix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(encodedImageUri: String) {
    val imageUri = Uri.parse(URLDecoder.decode(encodedImageUri, StandardCharsets.UTF_8.name()))
    val context = LocalContext.current

    // --- State for all editor properties ---
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var histogramData by remember { mutableStateOf<HistogramData?>(null) }

    // --- State for the filters panel ---
    var showFiltersSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val presets = remember { FilterLibrary.getPresets() }
    var selectedPreset by remember { mutableStateOf(presets.first()) }
    var filterIntensity by remember { mutableStateOf(100f) }


    // --- ADVANCED BLENDING LOGIC ---
    val finalColorFilter = remember(selectedPreset, filterIntensity) {
        val filterMatrix = selectedPreset.matrix
        val identityMatrix = AndroidColorMatrix()
        val interpolatedValues = FloatArray(20)
        for (i in 0..19) {
            interpolatedValues[i] = identityMatrix.array[i] + (filterMatrix.array[i] - identityMatrix.array[i]) * (filterIntensity / 100f)
        }
        val finalMatrix = AndroidColorMatrix(interpolatedValues)
        ColorFilter.colorMatrix(ComposeColorMatrix(finalMatrix.array))
    }

    // Load the bitmap and calculate the initial histogram
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            val bitmap = loadBitmapFromUri(imageUri, context)
            if (bitmap != null) {
                originalBitmap = bitmap
                histogramData = HistogramCalculator.calculate(bitmap)
            }
        }
    }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        val maxOffset = (containerSize.width * (newScale - 1) / 2f) to (containerSize.height * (newScale - 1) / 2f)
        val newOffset = (offset + offsetChange).let { Offset(x = it.x.coerceIn(-maxOffset.first, maxOffset.first), y = it.y.coerceIn(-maxOffset.second, maxOffset.second)) }
        scale = newScale
        offset = newOffset
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = showFiltersSheet,
                    onClick = { showFiltersSheet = true },
                    icon = { Icon(Icons.Outlined.FilterVintage, contentDescription = "Filters") },
                    label = { Text("Filters") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* TODO: Show adjust panel */ },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Adjust") },
                    label = { Text("Adjust") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .clipToBounds()
                .onSizeChanged { containerSize = it }
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Image to Edit",
                contentScale = ContentScale.Fit,
                colorFilter = finalColorFilter,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .transformable(state = transformableState)
            )

            HistogramView(
                histogramData = histogramData,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(0.dp)
                    .fillMaxWidth(0.5f)
                    .height(120.dp)
            )

            if (showFiltersSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFiltersSheet = false },
                    sheetState = sheetState
                ) {
                    FiltersPanel(
                        presets = presets,
                        selectedPreset = selectedPreset,
                        onPresetClick = { preset ->
                            selectedPreset = preset
                            if(preset.name != "None") {
                                filterIntensity = 100f
                            }
                        },
                        intensity = filterIntensity,
                        onIntensityChange = { intensity ->
                            filterIntensity = intensity
                        },
                        // --- THIS IS THE FIX ---
                        // We are now passing the reset action correctly
                        onResetIntensity = {
                            filterIntensity = 100f
                        }
                    )
                }
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
