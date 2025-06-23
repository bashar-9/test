package com.bashar.cinematicphotoeditor.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun EditorScreen(encodedImageUri: String) {
    // We still need to decode the URI that we passed during navigation.
    val imageUri = URLDecoder.decode(encodedImageUri, StandardCharsets.UTF_8.name())

    // The AsyncImage composable from the Coil library does all the hard work.
    // We just give it the 'model' (our imageUri) and it handles the rest.
    AsyncImage(
        model = imageUri,
        contentDescription = "Selected image for editing",
        modifier = Modifier.fillMaxSize() // The image will fill the entire screen.
    )
}