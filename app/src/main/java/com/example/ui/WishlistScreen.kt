package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.WishlistItem

@Composable
fun WishlistScreen(
    items: List<WishlistItem>,
    isLoading: Boolean = false,
    onBack: () -> Unit,
    onDelete: (Long) -> Unit,
    onItemClick: (WishlistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryGreen = Color(0xFF1B3D2F)
    val lightBackground = Color(0xFFF4F9F5)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(lightBackground)
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryGreen)
                .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "My Collection",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryGreen)
            }
        } else if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Eco,
                        contentDescription = null,
                        tint = primaryGreen.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Koleksi kamu masih kosong",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Scan tanaman dan tambahkan ke koleksi",
                        fontSize = 14.sp,
                        color = primaryGreen.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    WishlistCard(
                        item = item,
                        primaryGreen = primaryGreen,
                        onDelete = { onDelete(item.id) },
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun WishlistCard(
    item: WishlistItem,
    primaryGreen: Color,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Half: Green Shape with Placeholder Image/Icon
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .background(
                        color = primaryGreen,
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 30.dp, bottomEnd = 0.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
                val context = androidx.compose.ui.platform.LocalContext.current
                LaunchedEffect(item.id) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        val file = java.io.File(context.filesDir, "wishlist_image_${item.id}.jpg")
                        if (file.exists()) {
                            val bmp = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                            imageBitmap = bmp?.asImageBitmap()
                        }
                    }
                }

                if (imageBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = imageBitmap!!,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 30.dp, bottomEnd = 0.dp))
                    )
                } else {
                    Icon(
                        imageVector = if (item.scanMode == "DIAGNOSE") Icons.Outlined.Healing else Icons.Outlined.Eco,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Right Half: Information Details
            Box(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.commonName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!item.latinName.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.latinName,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = primaryGreen.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.matchPercentage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            AssistChip(
                                onClick = {},
                                label = { Text("${item.matchPercentage}% Match", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFFE2EEE6),
                                    labelColor = primaryGreen
                                ),
                                border = null,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(20.dp)
                            )
                        }
                    }
                }

                // Clean and minimal delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = Color(0xFFE53935).copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
