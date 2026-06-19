package com.example.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.PlantAnalysisResult
import com.example.models.ScanMode

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
        listOf("Diagnosis", "Penanganan", "Pencegahan")
    } else {
        listOf("Description", "Care Guide", "Origin")
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Background Image (Top Half)
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Captured Plant",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
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

        // Bottom Sheet (White Card)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
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
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 34.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (mode == ScanMode.DIAGNOSE) (result.commonName?.let { "Tanaman: $it" } ?: result.latinName ?: "") else (result.latinName ?: "Unknown Latin Name"),
                        fontSize = 18.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Match Percentage Circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .border(4.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result.matchPercentage ?: 0}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "MATCH",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                tabs.forEachIndexed { index, title ->
                    Column(
                        modifier = Modifier.clickable { selectedTab = index },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedTab == index) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(2.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant ?: Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Body and Tags (Scrollable)
            Column(modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())) {
                
                val bodyText = if (mode == ScanMode.DIAGNOSE) {
                    when (selectedTab) {
                        0 -> result.description ?: "Tidak ada detail diagnosis."
                        1 -> result.treatment ?: "Tidak ada petunjuk penanganan atau rekomendasi pestisida."
                        2 -> result.careGuide ?: "Tidak ada petunjuk pencegahan penyakit."
                        else -> ""
                    }
                } else {
                    when (selectedTab) {
                        0 -> result.description ?: "No description available."
                        1 -> result.careGuide ?: "No care guide available."
                        2 -> result.origin ?: "Origin unknown."
                        else -> ""
                    }
                }

                Text(
                    text = bodyText,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tags section
                if (!result.tags.isNullOrEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.tags.take(3).forEach { tag ->
                            val icon = when {
                                tag.contains("pet", true) -> Icons.Outlined.Pets
                                tag.contains("light", true) -> Icons.Outlined.BrightnessHigh
                                tag.contains("humid", true) -> Icons.Outlined.Opacity
                                else -> null
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text(tag, fontSize = 12.sp) },
                                leadingIcon = {
                                    if (icon != null) {
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                border = null,
                                shape = RoundedCornerShape(16.dp)
                            )
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to My Collection", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onRetake,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retake Photo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
