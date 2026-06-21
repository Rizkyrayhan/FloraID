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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Pets
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
import com.example.models.PlantAnalysisResult
import com.example.models.ScanMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    bitmap: Bitmap,
    result: PlantAnalysisResult,
    mode: ScanMode = ScanMode.IDENTIFY,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = if (mode == ScanMode.DIAGNOSE) {
        listOf("Diagnosis", "Care Guide", "Prevention")
    } else {
        listOf("Description", "Care Guide", "Origin")
    }

    // Colors from the screenshot
    val primaryGreen = Color(0xFF1B3D2F)
    val lightBackground = Color(0xFFE8F5E9)
    val floralRingColor = Color(0xFF2E5B48)

    Box(modifier = modifier.fillMaxSize().background(primaryGreen)) {
        // Background Image (Top Half)
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Captured Plant",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .align(Alignment.TopCenter)
        )

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
                        text = if (mode == ScanMode.DIAGNOSE && !result.diseaseName.isNullOrEmpty()) result.diseaseName else (result.commonName ?: "Janda Bolong"),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen,
                        lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (mode == ScanMode.DIAGNOSE) (result.commonName?.let { "Plant: $it" } ?: result.latinName ?: "") else (result.latinName ?: "Monstera adansonii"),
                        fontSize = 18.sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        color = primaryGreen.copy(alpha = 0.6f)
                    )
                }

                // Match Percentage Circle with Floral Ring
                MatchRing(
                    percentage = result.matchPercentage ?: 98,
                    color = floralRingColor,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs with Leaf Indicator
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
 
            // Body and Tags (Scrollable)
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
                        0 -> if (mode == ScanMode.DIAGNOSE && !result.diseaseName.isNullOrEmpty()) result.description ?: "" else result.description ?: ""
                        1 -> result.careGuide ?: "No care guide available."
                        2 -> if (mode == ScanMode.DIAGNOSE) result.treatment ?: "No prevention details available." else result.origin ?: "No origin details available."
                            else -> result.description ?: ""
                        }.ifEmpty { "Information not available." },
                        fontSize = 17.sp,
                        color = primaryGreen.copy(alpha = 0.8f),
                        lineHeight = 26.sp
                    )
     
                    Spacer(modifier = Modifier.height(24.dp))
     
                    // Tags section
                    FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val displayTags = if (!result.tags.isNullOrEmpty()) result.tags else listOf("Toxic to Pets", "Bright Indirect Light", "High Humidity")
                    displayTags.forEach { tag ->
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
                
                Spacer(modifier = Modifier.height(24.dp))
 
                // Action Buttons
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen, contentColor = Color.White),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Save to My Collection", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
 
@Composable
fun LeafIndicator(color: Color) {
    Canvas(modifier = Modifier.size(width = 60.dp, height = 18.dp)) {
        val w = size.width
        val h = size.height
        
        // Solid leaf path
        val leafColor = color.copy(alpha = 0.9f)
        val leafPath = Path().apply {
            moveTo(0f, h / 2)
            quadraticTo(w * 0.3f, 0f, w, h / 2)
            quadraticTo(w * 0.3f, h, 0f, h / 2)
            close()
        }
        drawPath(leafPath, leafColor)
        
        // Leaf veins (drawn as a darker stroke or cut out, we'll draw over with background color to simulate cutout)
        val veinColor = Color(0xFFE8F5E9) // Matching lightBackground
        val veinPath = Path().apply {
            moveTo(w * 0.1f, h / 2)
            lineTo(w * 0.9f, h / 2)
            
            // Branching veins
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
            
            // Draw a beautiful wreath of small leaves along the circle matching result.png
            val wreathRadius = size.minDimension / 2 - 10.dp.toPx()
            
            // Draw 22 leaves rotated along the circle
            val leafCount = 22
            for (i in 0 until leafCount) {
                val angleDegrees = (i * (360f / leafCount)) - 90f // Start from top
                val angleRad = Math.toRadians(angleDegrees.toDouble())
                
                val lx = cx + wreathRadius * Math.cos(angleRad).toFloat()
                val ly = cy + wreathRadius * Math.sin(angleRad).toFloat()
                
                // Draw a leaf path at (lx, ly) rotated to follow the circle
                val leafPath = Path()
                val rotationRad = Math.toRadians((angleDegrees + 45f).toDouble())
                val cos = Math.cos(rotationRad).toFloat()
                val sin = Math.sin(rotationRad).toFloat()
                
                val leafSize = 8.dp.toPx()
                val p0x = 0f
                val p0y = 0f
                val p1x = leafSize * 0.5f
                val p1y = -leafSize * 0.3f
                val p2x = leafSize
                val p2y = 0f
                val p3x = leafSize * 0.5f
                val p3y = leafSize * 0.3f
                
                fun rotX(px: Float, py: Float) = lx + px * cos - py * sin
                fun rotY(px: Float, py: Float) = ly + px * sin + py * cos
                
                leafPath.moveTo(rotX(p0x, p0y), rotY(p0x, p0y))
                leafPath.quadraticTo(rotX(p1x, p1y), rotY(p1x, p1y), rotX(p2x, p2y), rotY(p2x, p2y))
                leafPath.quadraticTo(rotX(p3x, p3y), rotY(p3x, p3y), rotX(p0x, p0y), rotY(p0x, p0y))
                leafPath.close()
                
                drawPath(leafPath, color)
            }
            
            // Subtle thin green circle inside the wreath
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = wreathRadius - 3.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage%",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = "MATCH",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
