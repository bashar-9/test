package com.bashar.cinematicphotoeditor.presentation

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bashar.cinematicphotoeditor.ui.theme.CinematicPhotoEditorTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    // The launcher is a property of the Activity itself. Simple and clean.
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // This part runs AFTER the user picks an image.
        if (uri != null) {
            // We get the NavController from our NavHost and tell it to navigate.
            val encodedUri = URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.name())
            navController?.navigate("editor_screen/$encodedUri")
        } else {
            Log.d("PhotoPicker", "No image selected")
        }
    }

    // A variable to hold our navigation controller.
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CinematicPhotoEditorTheme {
                // We create the NavController here and store it in our variable.
                val controller = rememberNavController()
                this.navController = controller
                // NavHost is the container that displays the current screen.
                NavHost(navController = controller, startDestination = "home_screen") {

                    // This is the route for our home screen.
                    composable("home_screen") {
                        HomeScreen(onUploadClick = {
                            // The button click now launches the photo picker.
                            pickImageLauncher.launch("image/*")
                        })
                    }

                    // This is the route for our editor screen.
                    // It expects an "imageUri" to be passed to it.
                    composable("editor_screen/{imageUri}") { backStackEntry ->
                        val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                        EditorScreen(encodedImageUri = imageUri)
                    }
                }
            }
        }
    }
}


@Composable
fun HomeScreen(onUploadClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Button(
                onClick = { onUploadClick() },
                modifier = Modifier
                    .padding(16.dp)
                    .height(56.dp)
                    .fillMaxWidth(0.7f)
            ) {
                Text(
                    text = "Upload Photo",
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CinematicPhotoEditorTheme {
        HomeScreen(onUploadClick = {})
    }
}

//testttt