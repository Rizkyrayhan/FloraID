package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AnalyzingScreen
import com.example.ui.AppState
import com.example.ui.CameraScreen
import com.example.ui.MainViewModel
import com.example.ui.ResultScreen
import com.example.ui.WishlistScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    if (cameraPermissionState.status.isGranted) {
                        AppHost(onClose = { finish() })
                    } else {
                        AlertDialog(
                            onDismissRequest = { },
                            title = { Text("Kamera Dibutuhkan") },
                            text = { Text("Aplikasi ini membutuhkan akses kamera untuk mendeteksi tanaman.") },
                            confirmButton = {
                                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                                    Text("Izinkan")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppHost(viewModel: MainViewModel = viewModel(), onClose: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()

    when (val state = uiState) {
        is AppState.Camera -> {
            val selectedMode by viewModel.scanMode.collectAsState()
            CameraScreen(
                onImageCaptured = { bitmap ->
                    viewModel.analyzeImage(bitmap)
                },
                onClose = onClose,
                selectedMode = selectedMode,
                onModeSelected = { mode -> viewModel.setScanMode(mode) },
                onOpenWishlist = { viewModel.showWishlist() }
            )
        }
        is AppState.Analyzing -> {
            var progress by remember { mutableFloatStateOf(0.1f) }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                while (progress < 0.9f) {
                    kotlinx.coroutines.delay(500)
                    progress += 0.1f
                }
            }
            AnalyzingScreen(
                progress = progress,
                onCancel = { viewModel.resetToCamera() }
            )
        }
        is AppState.Result -> {
            ResultScreen(
                bitmap = state.image,
                result = state.plantId,
                mode = state.mode,
                onRetake = { viewModel.resetToCamera() }
            )
        }
        is AppState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetToCamera() },
                title = { Text("Peringatan") },
                text = { Text(state.message) },
                confirmButton = {
                    Button(onClick = { viewModel.resetToCamera() }) {
                        Text("Coba Lagi")
                    }
                }
            )
        }
        is AppState.Wishlist -> {
            WishlistScreen(
                items = wishlistItems,
                onBack = { viewModel.resetToCamera() },
                onDelete = { id -> viewModel.deleteFromWishlist(id) },
                onItemClick = { item -> viewModel.openWishlistItem(item) }
            )
        }
    }
}
