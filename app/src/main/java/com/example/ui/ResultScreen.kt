package com.example.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.models.PlantAnalysisResult
import com.example.models.ScanMode

@Composable
fun ResultScreen(
    bitmap: Bitmap?,
    result: PlantAnalysisResult,
    mode: ScanMode = ScanMode.IDENTIFY,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: MainViewModel = viewModel()
    val saveState by viewModel.wishlistSaveState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = if (mode == ScanMode.DIAGNOSE) {
        listOf("Diagnosis", "Care Guide", "Prevention")
    } else {
        listOf("Description", "Care Guide", "Origin")
    }

    // Colors
    val primaryGreen = Color(0xFF1B3D2F)
    val lightBackground = Color(0xFFE8F5E9)
    val floralRingColor = Color(0xFF2E5B48)

    // Snackbar for feedback
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (saveState) {
            is WishlistSaveState.Success -> {
                snackbarHostState.showSnackbar("Berhasil disimpan ke Wishlist!")
                viewModel.resetSaveState()
            }
            is WishlistSaveState.Error -> {
                snackbarHostState.showSnackbar("Gagal menyimpan: ${(saveState as WishlistSaveState.Error).message}")
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = modifier.fillMaxSize().background(primaryGreen).padding(innerPadding)) {
            // Background Image (Top Half)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured Plant",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.45f)
                        .align(Alignment.TopCenter)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.45f)
                        .align(Alignment.TopCenter)
                        .background(primaryGreen.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (mode == ScanMode.DIAGNOSE) Icons.Outlined.Healing else Icons.Outlined.Eco,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(140.dp)
                    )
                }
            }

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onRetake,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                IconButton(
                    onClick = { /* TODO share */ },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
            }

            // Bottom Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .align(Alignment.BottomCenter)
                    .background(lightBackground, RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .padding(top = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (mode == ScanMode.DIAGNOSE && !result.diseaseName.isNullOrEmpty()) result.diseaseName else (result.commonName ?: "Unknown Plant"),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen,
                            lineHeight = 36.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (mode == ScanMode.DIAGNOSE) (result.commonName?.let { "Plant: $it" } ?: result.latinName ?: "") else (result.latinName ?: ""),
                            fontSize = 18.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            color = primaryGreen.copy(alpha = 0.6f)
                        )
                    }

                    // Match Percentage Circle
                    MatchRing(
                        percentage = result.matchPercentage ?: 98,
                        color = floralRingColor,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tabs
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    tabs.forEachIndexed { index, title ->
                        Column(
                            modifier = Modifier.clickable { selectedTab = index },
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) primaryGreen else primaryGreen.copy(alpha = 0.5f)
                            )
                            if (selectedTab == index) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LeafIndicator(color = primaryGreen)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = primaryGreen.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content
                Column(modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())) {

                    Box {
                        // Leaf Watermark
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val path = Path().apply {
                                moveTo(size.width * 0.8f, size.height)
                                quadraticTo(size.width * 0.2f, size.height * 0.8f, 0f, size.height * 0.3f)
                                quadraticTo(size.width * 0.1f, 0f, size.width * 0.7f, 0f)
                                quadraticTo(size.width, size.height * 0.2f, size.width * 0.8f, size.height)
                                moveTo(size.width * 0.8f, size.height)
                                lineTo(size.width * 0.3f, size.height * 0.2f)
                                moveTo(size.width * 0.6f, size.height * 0.7f)
                                lineTo(size.width * 0.3f, size.height * 0.5f)
                                moveTo(size.width * 0.5f, size.height * 0.5f)
                                lineTo(size.width * 0.2f, size.height * 0.35f)
                            }
                            drawPath(path, Color(0xFFD6E8DB).copy(alpha = 0.4f), style = Stroke(width = 4.dp.toPx()))
                        }

                        Column {
                            Text(
                                text = when (selectedTab) {
                                    0 -> result.description ?: ""
                                    1 -> result.careGuide ?: "No care guide available."
                                    2 -> if (mode == ScanMode.DIAGNOSE) result.treatment ?: "No prevention details available." else result.origin ?: "No origin details available."
                                    else -> result.description ?: ""
                                }.ifEmpty { "Information not available." },
                                fontSize = 17.sp,
                                color = primaryGreen.copy(alpha = 0.8f),
                                lineHeight = 26.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Tags section — using manual wrapping instead of FlowRow
                            val displayTags = if (!result.tags.isNullOrEmpty()) result.tags else listOf("Bright Indirect Light", "High Humidity")
                            TagsSection(tags = displayTags, primaryGreen = primaryGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save to Wishlist Button
                    val isSaving = saveState is WishlistSaveState.Saving
                    Button(
                        onClick = { viewModel.saveToWishlist(result, mode) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Menyimpan...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Save to My Collection", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onRetake,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryGreen),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, primaryGreen)
                    ) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Retake Photo", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun TagsSection(tags: List<String>, primaryGreen: Color) {
    // Manual row-wrapping for chips (replaces FlowRow which has API incompatibility)
    val rows = mutableListOf<MutableList<String>>()
    var currentRow = mutableListOf<String>()
    tags.forEachIndexed { index, tag ->
        currentRow.add(tag)
        if (currentRow.size == 2 || index == tags.size - 1) {
            rows.add(currentRow)
            currentRow = mutableListOf()
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowTags ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTags.forEach { tag ->
                    val icon = when {
                        tag.contains("pet", true) -> Icons.Outlined.Pets
                        tag.contains("light", true) -> Icons.Outlined.BrightnessHigh
                        tag.contains("humid", true) -> Icons.Outlined.Opacity
                        else -> null
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text(tag, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            if (icon != null) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFE2EEE6),
                            labelColor = primaryGreen,
                            leadingIconContentColor = primaryGreen
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LeafIndicator(color: Color) {
    Canvas(modifier = Modifier.size(width = 60.dp, height = 18.dp)) {
        val w = size.width
        val h = size.height
        val leafColor = color.copy(alpha = 0.9f)
        val leafPath = Path().apply {
            moveTo(0f, h / 2)
            quadraticTo(w * 0.3f, 0f, w, h / 2)
            quadraticTo(w * 0.3f, h, 0f, h / 2)
            close()
        }
        drawPath(leafPath, leafColor)
        val veinColor = Color(0xFFE8F5E9)
        val veinPath = Path().apply {
            moveTo(w * 0.1f, h / 2)
            lineTo(w * 0.9f, h / 2)
            moveTo(w * 0.3f, h / 2)
            lineTo(w * 0.5f, h * 0.2f)
            moveTo(w * 0.4f, h / 2)
            lineTo(w * 0.6f, h * 0.8f)
            moveTo(w * 0.6f, h / 2)
            lineTo(w * 0.8f, h * 0.3f)
        }
        drawPath(veinPath, veinColor, style = Stroke(width = 1.5.dp.toPx()))
    }
}

@Composable
fun MatchRing(percentage: Int, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2
            val cy = h / 2
            val wreathRadius = size.minDimension / 2 - 10.dp.toPx()
            val leafCount = 22
            for (i in 0 until leafCount) {
                val angleDegrees = (i * (360f / leafCount)) - 90f
                val angleRad = Math.toRadians(angleDegrees.toDouble())
                val lx = cx + wreathRadius * Math.cos(angleRad).toFloat()
                val ly = cy + wreathRadius * Math.sin(angleRad).toFloat()
                val leafPath = Path()
                val rotationRad = Math.toRadians((angleDegrees + 45f).toDouble())
                val cos = Math.cos(rotationRad).toFloat()
                val sin = Math.sin(rotationRad).toFloat()
                val leafSize = 8.dp.toPx()
                val p0x = 0f; val p0y = 0f
                val p1x = leafSize * 0.5f; val p1y = -leafSize * 0.3f
                val p2x = leafSize; val p2y = 0f
                val p3x = leafSize * 0.5f; val p3y = leafSize * 0.3f
                fun rotX(px: Float, py: Float) = lx + px * cos - py * sin
                fun rotY(px: Float, py: Float) = ly + px * sin + py * cos
                leafPath.moveTo(rotX(p0x, p0y), rotY(p0x, p0y))
                leafPath.quadraticTo(rotX(p1x, p1y), rotY(p1x, p1y), rotX(p2x, p2y), rotY(p2x, p2y))
                leafPath.quadraticTo(rotX(p3x, p3y), rotY(p3x, p3y), rotX(p0x, p0y), rotY(p0x, p0y))
                leafPath.close()
                drawPath(leafPath, color)
            }
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = wreathRadius - 3.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$percentage%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = "MATCH", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
