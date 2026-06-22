package com.example.ui

import android.graphics.Bitmap
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.PlantAnalysisResult
import com.example.models.ScanMode
import com.example.models.WishlistItem
import com.example.repository.PlantRepository
import com.example.repository.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AppState {
    object Camera : AppState()
    object Analyzing : AppState()
    data class Result(val plantId: PlantAnalysisResult, val image: Bitmap?, val mode: ScanMode) : AppState()
    data class Error(val message: String) : AppState()
    object Wishlist : AppState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlantRepository()
    private val wishlistRepository = WishlistRepository()

    private val _uiState = MutableStateFlow<AppState>(AppState.Camera)
    val uiState: StateFlow<AppState> = _uiState

    private val _scanMode = MutableStateFlow(ScanMode.IDENTIFY)
    val scanMode: StateFlow<ScanMode> = _scanMode

    private val _wishlistItems = MutableStateFlow<List<WishlistItem>>(emptyList())
    val wishlistItems: StateFlow<List<WishlistItem>> = _wishlistItems

    private val _wishlistSaveState = MutableStateFlow<WishlistSaveState>(WishlistSaveState.Idle)
    val wishlistSaveState: StateFlow<WishlistSaveState> = _wishlistSaveState

    fun setScanMode(mode: ScanMode) {
        _scanMode.value = mode
    }

    fun analyzeImage(bitmap: Bitmap) {
        val currentMode = _scanMode.value
        _uiState.value = AppState.Analyzing
        viewModelScope.launch {
            val result = repository.analyzePlant(bitmap, currentMode)
            if (result.isSuccess) {
                val data = result.getOrThrow()
                if (data.isPlant && (data.matchPercentage ?: 0) >= 65) {
                    _uiState.value = AppState.Result(data, bitmap, currentMode)
                } else {
                    _uiState.value = AppState.Error("Objek kurang jelas. Pastikan hanya mengambil satu daun utama dengan pencahayaan cukup.")
                }
            } else {
                _uiState.value = AppState.Error(result.exceptionOrNull()?.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun saveToWishlist(result: PlantAnalysisResult, mode: ScanMode) {
        val state = _uiState.value as? AppState.Result
        val bitmap = state?.image

        viewModelScope.launch {
            _wishlistSaveState.value = WishlistSaveState.Saving
            val item = WishlistItem(
                commonName = result.commonName ?: result.diseaseName ?: "Unknown",
                latinName = result.latinName,
                description = result.description,
                matchPercentage = result.matchPercentage,
                tags = result.tags?.joinToString(","),
                scanMode = mode.name
            )
            val saveResult = wishlistRepository.saveToWishlist(item)
            if (saveResult.isSuccess) {
                val insertedItem = saveResult.getOrThrow()
                if (bitmap != null) {
                    try {
                        val file = java.io.File(getApplication<Application>().filesDir, "wishlist_image_${insertedItem.id}.jpg")
                        java.io.FileOutputStream(file).use { out ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                _wishlistSaveState.value = WishlistSaveState.Success
            } else {
                _wishlistSaveState.value = WishlistSaveState.Error(saveResult.exceptionOrNull()?.message ?: "Gagal menyimpan")
            }
        }
    }

    fun resetSaveState() {
        _wishlistSaveState.value = WishlistSaveState.Idle
    }

    fun loadWishlist() {
        viewModelScope.launch {
            val result = wishlistRepository.getWishlist()
            if (result.isSuccess) {
                _wishlistItems.value = result.getOrThrow()
            }
        }
    }

    fun deleteFromWishlist(id: Long) {
        viewModelScope.launch {
            wishlistRepository.deleteFromWishlist(id)
            _wishlistItems.value = _wishlistItems.value.filter { it.id != id }
        }
    }

    fun showWishlist() {
        loadWishlist()
        _uiState.value = AppState.Wishlist
    }

    fun openWishlistItem(item: WishlistItem) {
        val result = PlantAnalysisResult(
            isPlant = true,
            commonName = item.commonName,
            latinName = item.latinName,
            description = item.description,
            matchPercentage = item.matchPercentage,
            tags = item.tags?.split(",")?.map { it.trim() },
            diseaseName = if (item.scanMode == "DIAGNOSE") item.commonName else null,
            treatment = null,
            careGuide = null,
            origin = null
        )
        val mode = if (item.scanMode == "DIAGNOSE") ScanMode.DIAGNOSE else ScanMode.IDENTIFY
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val file = java.io.File(getApplication<Application>().filesDir, "wishlist_image_${item.id}.jpg")
            val bitmap = if (file.exists()) {
                android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            } else null
            
            _uiState.value = AppState.Result(result, bitmap, mode)
        }
    }

    fun resetToCamera() {
        _uiState.value = AppState.Camera
    }
}

sealed class WishlistSaveState {
    object Idle : WishlistSaveState()
    object Saving : WishlistSaveState()
    object Success : WishlistSaveState()
    data class Error(val message: String) : WishlistSaveState()
}
