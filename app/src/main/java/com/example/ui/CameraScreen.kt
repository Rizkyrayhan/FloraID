package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.Image as ImageIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.models.ScanMode
import java.util.concurrent.Executor


@Composable
fun CameraScreen(
    onImageCaptured: (Bitmap) -> Unit,
    onClose: () -> Unit = {},
    selectedMode: ScanMode = ScanMode.IDENTIFY,
    onModeSelected: (ScanMode) -> Unit = {},
    onOpenWishlist: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    onImageCaptured(bitmap)
                }
            } catch (e: Exception) {
                Log.e("Gallery", "Failed to load image from gallery: ${e.message}", e)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val executor = ContextCompat.getMainExecutor(ctx)
                setupCamera(ctx, lifecycleOwner, previewView, executor) { capture, cam ->
                    imageCapture = capture
                    camera = cam
                }
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Floral Overlay Guide
        CameraOverlay(modifier = Modifier.fillMaxSize())

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close Button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Wishlist / Collection Button
            IconButton(
                onClick = onOpenWishlist,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = "Buka Koleksi",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom Bar Area
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                        startY = 0f
                    )
                )
        ) {
            // Leaf graphic background behind button (visual fluff)
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
            ) {
                val path = Path().apply {
                    moveTo(0f, size.height)
                    quadraticTo(size.width / 2, size.height - 120.dp.toPx(), size.width, size.height)
                    close()
                }
                drawPath(path, Color.White.copy(alpha = 0.2f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp, start = 48.dp, end = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Icon
                IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(
                        Icons.Outlined.ImageIcon,
                        contentDescription = "Pilih dari Galeri",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Main Capture Button
                CaptureButton(
                    onClick = {
                        takePhoto(imageCapture, context, ContextCompat.getMainExecutor(context), onImageCaptured)
                    }
                )

                // Flash Toggle Icon
                IconButton(onClick = {
                    isFlashOn = !isFlashOn
                    camera?.cameraControl?.enableTorch(isFlashOn)
                }) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = if (isFlashOn) "Matikan Flash" else "Nyalakan Flash",
                        tint = if (isFlashOn) Color(0xFFFFEB3B) else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CaptureButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(84.dp)
            .border(3.dp, Color.White, CircleShape)
            .padding(6.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.9f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            val path = Path().apply {
                moveTo(size.width / 2, size.height)
                quadraticTo(0f, size.height * 0.8f, 0f, size.height * 0.4f)
                quadraticTo(size.width * 0.2f, 0f, size.width / 2, 0f)
                quadraticTo(size.width * 0.8f, 0f, size.width, size.height * 0.4f)
                quadraticTo(size.width, size.height * 0.8f, size.width / 2, size.height)
                moveTo(size.width / 2, size.height)
                lineTo(size.width / 2, size.height * 0.1f)
                moveTo(size.width / 2, size.height * 0.7f)
                lineTo(size.width * 0.2f, size.height * 0.5f)
                moveTo(size.width / 2, size.height * 0.5f)
                lineTo(size.width * 0.8f, size.height * 0.3f)
                moveTo(size.width / 2, size.height * 0.3f)
                lineTo(size.width * 0.25f, size.height * 0.15f)
            }
            drawPath(path, Color.LightGray, style = Stroke(width = 1.5.dp.toPx()))
        }
    }
}

@Composable
fun CameraOverlay(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Decorative Floral Frame
        FloralFrame(
            modifier = Modifier
                .size(width = 280.dp, height = 360.dp)
                .padding(bottom = 40.dp)
        )

        // Instruction Text
        Text(
            text = "Align leaf within the frame",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 170.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 24.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun FloralFrame(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.dp.toPx()
        val color = Color(0xFFC5E1A5) // Light Green

        // Draw leafy corners
        val cornerPaths = listOf(
            // Top Left
            Path().apply {
                moveTo(40.dp.toPx(), 0f)
                quadraticTo(0f, 0f, 0f, 40.dp.toPx())
                for (i in 0..3) {
                    val offset = i * 12.dp.toPx()
                    moveTo(offset, 0f)
                    quadraticTo(offset + 6.dp.toPx(), -8.dp.toPx(), offset + 12.dp.toPx(), 0f)
                    moveTo(0f, offset)
                    quadraticTo(-8.dp.toPx(), offset + 6.dp.toPx(), 0f, offset + 12.dp.toPx())
                }
            },
            // Top Right
            Path().apply {
                moveTo(w - 40.dp.toPx(), 0f)
                quadraticTo(w, 0f, w, 40.dp.toPx())
                for (i in 0..3) {
                    val offset = i * 12.dp.toPx()
                    moveTo(w - offset, 0f)
                    quadraticTo(w - offset - 6.dp.toPx(), -8.dp.toPx(), w - offset - 12.dp.toPx(), 0f)
                    moveTo(w, offset)
                    quadraticTo(w + 8.dp.toPx(), offset + 6.dp.toPx(), w, offset + 12.dp.toPx())
                }
            },
            // Bottom Right
            Path().apply {
                moveTo(w, h - 40.dp.toPx())
                quadraticTo(w, h, w - 40.dp.toPx(), h)
                for (i in 0..3) {
                    val offset = i * 12.dp.toPx()
                    moveTo(w, h - offset)
                    quadraticTo(w + 8.dp.toPx(), h - offset - 6.dp.toPx(), w, h - offset - 12.dp.toPx())
                    moveTo(w - offset, h)
                    quadraticTo(w - offset - 6.dp.toPx(), h + 8.dp.toPx(), w - offset - 12.dp.toPx(), h)
                }
            },
            // Bottom Left
            Path().apply {
                moveTo(40.dp.toPx(), h)
                quadraticTo(0f, h, 0f, h - 40.dp.toPx())
                for (i in 0..3) {
                    val offset = i * 12.dp.toPx()
                    moveTo(offset, h)
                    quadraticTo(offset + 6.dp.toPx(), h + 8.dp.toPx(), offset + 12.dp.toPx(), h)
                    moveTo(0f, h - offset)
                    quadraticTo(-8.dp.toPx(), h - offset - 6.dp.toPx(), 0f, h - offset - 12.dp.toPx())
                }
            }
        )

        cornerPaths.forEach { path ->
            drawPath(path, color, style = Stroke(width = stroke))
        }

        // Center crosshair (subtle)
        val cx = w / 2
        val cy = h / 2
        drawLine(color.copy(alpha = 0.5f), Offset(cx - 10.dp.toPx(), cy), Offset(cx + 10.dp.toPx(), cy), stroke)
        drawLine(color.copy(alpha = 0.5f), Offset(cx, cy - 10.dp.toPx()), Offset(cx, cy + 10.dp.toPx()), stroke)
    }
}

private fun setupCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    executor: Executor,
    onImageCaptureReady: (ImageCapture, Camera) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            onImageCaptureReady(imageCapture, camera)
        } catch (e: Exception) {
            Log.e("Camera", "Use case binding failed", e)
        }
    }, executor)
}

private fun takePhoto(
    imageCapture: ImageCapture?,
    context: Context,
    executor: Executor,
    onImageCaptured: (Bitmap) -> Unit
) {
    val capture = imageCapture ?: return

    capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val bitmap = imageProxyToBitmap(image)
            image.close()
            onImageCaptured(bitmap)
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("Camera", "Photo capture failed: ${exception.message}", exception)
        }
    })
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
