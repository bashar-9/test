package com.bashar.cinematicphotoeditor.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bashar.cinematicphotoeditor.domain.BitmapFilterer
import com.bashar.cinematicphotoeditor.domain.HistogramCalculator
import com.bashar.cinematicphotoeditor.domain.HistogramData
import com.bashar.cinematicphotoeditor.domain.ImageSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.compose.ui.graphics.ColorMatrix as ComposeColorMatrix
import android.graphics.ColorMatrix as AndroidColorMatrix

private enum class ActivePanel {
    NONE, FILTERS, ADJUST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(encodedImageUri: String, navController: NavController) {
    val imageUri = Uri.parse(URLDecoder.decode(encodedImageUri, StandardCharsets.UTF_8.name()))
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- State for editor properties ---
    var activePanel by remember { mutableStateOf(ActivePanel.NONE) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var histogramData by remember { mutableStateOf<HistogramData?>(null) }
    var isPreviewingOriginal by remember { mutableStateOf(false) }

    // --- State for Filters Panel ---
    val presets = remember { FilterLibrary.getPresets() }
    var selectedPreset by remember { mutableStateOf(presets.first()) }
    var filterIntensity by remember { mutableStateOf(100f) }

    // --- State for Adjustments Panel ---
    val adjustmentTools = remember { AdjustmentTools.getTools() }
    var selectedTool by remember { mutableStateOf<AdjustmentTool?>(null) }
    var adjustmentValues by remember { mutableStateOf<Map<String, Float>>(adjustmentTools.associate { it.name to it.initialValue }) }

    // --- NEW: State for loading indicator during save ---
    var isSaving by remember { mutableStateOf(false) }


    // --- The Final Combined ColorMatrix Logic ---
    val finalAndroidMatrix = remember(selectedPreset, filterIntensity, adjustmentValues) {
        // We only calculate the matrix once here
        val presetMatrix = selectedPreset.matrix
        val identityMatrix = AndroidColorMatrix()
        val interpolatedValues = FloatArray(20)
        for (i in 0..19) {
            interpolatedValues[i] = identityMatrix.array[i] + (presetMatrix.array[i] - identityMatrix.array[i]) * (filterIntensity / 100f)
        }
        val finalMatrix = AndroidColorMatrix(interpolatedValues)

        val contrast = adjustmentValues["Contrast"] ?: 1.0f
        val contrastMatrix = AndroidColorMatrix().apply { setScale(contrast, contrast, contrast, 1f) }
        finalMatrix.postConcat(contrastMatrix)

        val exposure = adjustmentValues["Exposure"] ?: 0.0f
        val exposureMatrix = AndroidColorMatrix().apply {
            val brightness = exposure * 255
            set(floatArrayOf(1f,0f,0f,0f,brightness, 0f,1f,0f,0f,brightness, 0f,0f,1f,0f,brightness, 0f,0f,0f,1f,0f))
        }
        finalMatrix.postConcat(exposureMatrix)

        val saturation = adjustmentValues["Saturation"] ?: 1.0f
        val saturationMatrix = AndroidColorMatrix().apply{ setSaturation(saturation) }
        finalMatrix.postConcat(saturationMatrix)

        finalMatrix
    }

    // The ColorFilter for the UI is derived from the final matrix
    val finalColorFilter = remember(finalAndroidMatrix, isPreviewingOriginal) {
        if (isPreviewingOriginal) null else ColorFilter.colorMatrix(ComposeColorMatrix(finalAndroidMatrix.array))
    }

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
        topBar = {
            TopAppBar(
                title = { Text("Edit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // --- UPDATED: Save Button Logic ---
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSaving = true
                                val bitmapToSave = originalBitmap
                                if (bitmapToSave != null) {
                                    val savedSuccessfully = withContext(Dispatchers.IO) {
                                        // Apply the final edits and save the image
                                        val finalBitmap = BitmapFilterer.applyColorMatrix(bitmapToSave, finalAndroidMatrix)
                                        ImageSaver.saveBitmap(context, finalBitmap, "EditedImage")
                                    }
                                    if (savedSuccessfully) {
                                        Toast.makeText(context, "Image saved successfully!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                isSaving = false
                            }
                        },
                        enabled = !isSaving // Disable button while saving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = activePanel == ActivePanel.FILTERS, onClick = { activePanel = if (activePanel == ActivePanel.FILTERS) ActivePanel.NONE else ActivePanel.FILTERS }, icon = { Icon(Icons.Outlined.FilterVintage, contentDescription = "Filters") }, label = { Text("Filters") })
                NavigationBarItem(selected = activePanel == ActivePanel.ADJUST, onClick = { activePanel = if (activePanel == ActivePanel.ADJUST) ActivePanel.NONE else ActivePanel.ADJUST }, icon = { Icon(Icons.Default.Tune, contentDescription = "Adjust") }, label = { Text("Adjust") })
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).clipToBounds()
                    .onSizeChanged { containerSize = it }
                    .pointerInput(Unit) { detectTapGestures(onPress = { isPreviewingOriginal = true; try { awaitRelease() } finally { isPreviewingOriginal = false } }) }
            ) {
                AsyncImage(
                    model = imageUri, contentDescription = "Image to Edit", contentScale = ContentScale.Fit, colorFilter = finalColorFilter,
                    modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y }.transformable(state = transformableState)
                )
                HistogramView(
                    histogramData = histogramData,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(8.dp).fillMaxWidth(0.4f).height(120.dp)
                )
            }
            AnimatedVisibility(visible = activePanel == ActivePanel.FILTERS, enter = slideInVertically(initialOffsetY = { it }), exit = slideOutVertically(targetOffsetY = { it })) {
                FiltersPanel(presets = presets, selectedPreset = selectedPreset, onPresetClick = { preset -> selectedPreset = preset; if(preset.name != "None") { filterIntensity = 100f } }, intensity = filterIntensity, onIntensityChange = { intensity -> filterIntensity = intensity }, onResetIntensity = { filterIntensity = 100f })
            }
            AnimatedVisibility(visible = activePanel == ActivePanel.ADJUST, enter = slideInVertically(initialOffsetY = { it }), exit = slideOutVertically(targetOffsetY = { it })) {
                AdjustmentsPanel(tools = adjustmentTools, selectedTool = selectedTool, onToolClick = { tool -> selectedTool = tool }, adjustmentValues = adjustmentValues, onAdjustmentChange = { toolName, newValue -> adjustmentValues = adjustmentValues.toMutableMap().apply { this[toolName] = newValue } }, onResetAdjustment = { toolName -> val initialValue = adjustmentTools.find { it.name == toolName }?.initialValue; if(initialValue != null) { adjustmentValues = adjustmentValues.toMutableMap().apply { this[toolName] = initialValue } } })
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
