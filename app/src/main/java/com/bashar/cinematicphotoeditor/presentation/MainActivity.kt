package com.bashar.cinematicphotoeditor.presentation

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// import androidx.activity.enableEdgeToEdge // This should be commented out or removed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bashar.cinematicphotoeditor.ui.theme.CinematicPhotoEditorTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CinematicPhotoEditorTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home_screen") {
                    composable("home_screen") {
                        HomeScreen(
                            onImageClick = { uri ->
                                val encodedUri = URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.name())
                                navController.navigate("editor_screen/$encodedUri")
                            }
                        )
                    }

                    composable("editor_screen/{imageUri}") { backStackEntry ->
                        val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                        EditorScreen(
                            encodedImageUri = imageUri,
                            // --- THIS IS THE ONLY CHANGE ---
                            // We now pass the NavController to the EditorScreen
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
