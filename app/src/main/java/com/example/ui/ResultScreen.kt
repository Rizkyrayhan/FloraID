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
import androidx.compose.material.icons.outlined.BrightnessHigh
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
import androidx.compose.ui.res.painterResource
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
                modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            IconButton(
                onClick = { /* TODO share */ },
                modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
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
                
                Text(
                    text = result.description ?: "A striking and highly sought-after aroid, celebrated for its unique fenestrations—the characteristic oval holes that develop naturally in its vibrant green leaves. These adaptive fenestrations help the plant withstand strong winds and allow light to pass through to lower foliage in its natural tropical habitat.\n\nOften cultivated as an indoor climbing or trailing plant, it brings a bold, jungle-like aesthetic to interior spaces while remaining remarkably forgiving for domestic gardeners.",
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
                                containerColor = Color(0xFFCFE2D4),
                                labelColor = primaryGreen,
                                leadingIconContentColor = primaryGreen
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen, contentColor = Color.White),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Icon(painter = painterResource(android.R.drawable.ic_menu_save), contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Save to My Collection", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onRetake,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryGreen)
                ) {
                    Icon(painter = painterResource(android.R.drawable.ic_menu_camera), contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Retake Photo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun LeafIndicator(color: Color) {
    Canvas(modifier = Modifier.size(width = 60.dp, height = 24.dp)) {
        val path = Path().apply {
            moveTo(0f, 10f)
            quadraticTo(size.width * 0.4f, -5f, size.width * 0.8f, 15f)
            quadraticTo(size.width * 0.4f, 25f, 0f, 10f)
            close()
            // Stem line
            moveTo(0f, 10f)
            lineTo(size.width * 0.6f, 12f)
        }
        drawPath(path, color)
    }
}

@Composable
fun MatchRing(percentage: Int, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            
            // Draw floral pattern ring
            drawCircle(
                color = color.copy(alpha = 0.2f),
                radius = size.minDimension / 2 - strokeWidth,
                style = Stroke(width = 1.dp.toPx())
            )
            
            val radius = size.minDimension / 2 - 10.dp.toPx()
            for (i in 0 until 12) {
                val angle = (i * 30).toDouble()
                val x = center.x + radius * Math.cos(Math.toRadians(angle)).toFloat()
                val y = center.y + radius * Math.sin(Math.toRadians(angle)).toFloat()
                
                drawCircle(color, 3.dp.toPx(), Offset(x, y))
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage%",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = "MATCH",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
