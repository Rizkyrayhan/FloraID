package com.example.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.PlantAnalysisResult
import com.example.models.ScanMode
import com.example.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AppState {
    object Camera : AppState()
    object Analyzing : AppState()
    data class Result(val plantId: PlantAnalysisResult, val image: Bitmap, val mode: ScanMode) : AppState()
    data class Error(val message: String) : AppState()
}

class MainViewModel : ViewModel() {
    private val repository = PlantRepository()

    private val _uiState = MutableStateFlow<AppState>(AppState.Camera)
    val uiState: StateFlow<AppState> = _uiState

    private val _scanMode = MutableStateFlow(ScanMode.IDENTIFY)
    val scanMode: StateFlow<ScanMode> = _scanMode

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

    fun resetToCamera() {
        _uiState.value = AppState.Camera
    }
}
