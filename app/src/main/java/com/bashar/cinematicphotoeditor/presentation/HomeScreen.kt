package com.bashar.cinematicphotoeditor.presentation

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onImageClick: (Uri) -> Unit,
    homeViewModel: HomeViewModel = viewModel() // This line should now work correctly
) {
    val images by homeViewModel.images.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                homeViewModel.loadImages(context)
            }
        }
    )

    // On start, request the permission
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cinematic Photo Editor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(4.dp)
        ) {
            // The items call now correctly infers that 'images' is a List<Uri>
            items(images) { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Gallery Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clickable { onImageClick(uri) }
                )
            }
        }
    }
}
