package com.example.repository

import android.graphics.Bitmap
import android.util.Base64
import com.example.models.PlantAnalysisResult
import com.example.models.ScanMode
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.InlineData
import com.example.network.Part
import com.example.network.RetrofitClient
import com.example.network.Schema
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class PlantRepository {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(PlantAnalysisResult::class.java)

    suspend fun analyzePlant(bitmap: Bitmap, mode: ScanMode): Result<PlantAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val base64Image = bitmap.toBase64()
            
            val schema = Schema(
                type = "OBJECT",
                properties = mapOf(
                    "isPlant" to Schema(type = "BOOLEAN", description = "True if the image contains a plant, leaf, or part of a plant."),
                    "matchPercentage" to Schema(type = "INTEGER", description = "Confidence score of the analysis/diagnosis from 0 to 100."),
                    "commonName" to Schema(type = "STRING", description = "Common name of the plant (in Indonesian)."),
                    "latinName" to Schema(type = "STRING", description = "Latin/Scientific name of the plant."),
                    "description" to Schema(type = "STRING", description = "A paragraph describing the plant (or disease symptoms & general info if in diagnosis mode)."),
                    "careGuide" to Schema(type = "STRING", description = "How to care for this plant or prevent this disease (in Indonesian)."),
                    "origin" to Schema(type = "STRING", description = "Region/habitat of origin, or environmental notes (in Indonesian)."),
                    "tags" to Schema(
                        type = "ARRAY",
                        items = Schema(type = "STRING"),
                        description = "Tags related to plant status (e.g., 'Terinfeksi', 'Bercak Daun', 'Sehat', 'Suhu Hangat', 'Pestisida')."
                    ),
                    "diseaseName" to Schema(type = "STRING", description = "Indonesian name of the identified disease or pest (e.g. 'Karat Daun', 'Bercak Hitam', 'Hama Kutu'). Set to null if healthy."),
                    "hasDisease" to Schema(type = "BOOLEAN", description = "True if a plant disease, fungal infestation, or pest is detected. False otherwise."),
                    "treatment" to Schema(type = "STRING", description = "Detail penanganan penyakit tanaman serta rekomendasi pestisida yang harus digunakan (dalam Bahasa Indonesia). Set to null if healthy."),
                    "isHealthy" to Schema(type = "BOOLEAN", description = "True if the plant looks generally healthy. False if it shows signs of poor health, stress, or is dead."),
                    "isDead" to Schema(type = "BOOLEAN", description = "True if the plant appears completely dead or beyond recovery."),
                    "healthAssessment" to Schema(type = "STRING", description = "A brief assessment of the plant's overall health condition in Indonesian (e.g. 'Sehat', 'Stres karena kurang air', 'Mati')."),
                    "healthSolution" to Schema(type = "STRING", description = "Solution to improve the plant's health if it's not healthy (in Indonesian). Set to null if healthy or dead.")
                )
            )

            val promptText = when (mode) {
                ScanMode.IDENTIFY -> "Analyze this image and identify the plant. Assess its overall health (isHealthy, isDead) and provide healthAssessment and healthSolution if needed. Return structural details based on the schema."
                ScanMode.DIAGNOSE -> "Analyze this plant leaf image to detect diseases, pests, fungal infections, nutrient deficiencies, or general health issues. Assess if the plant is dead. Set hasDisease to true if any issue is detected, identify the diseaseName in Indonesian, and specify detailed treatment and pesticide recommendations in Indonesian. Also evaluate overall health (isHealthy, isDead) and provide healthAssessment and healthSolution in Indonesian."
            }

            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = promptText),
                            Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    responseSchema = schema,
                    temperature = 0.2f
                )
            )

            val response = RetrofitClient.service.generateContent(request = request)
            val jsonResponseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("No response text from AI")

            val plantData = adapter.fromJson(jsonResponseText) 
                ?: throw Exception("Failed to parse JSON")

            Result.success(plantData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        // Resize bitmap to avoid sending too large payloads to Gemini API
        val maxDim = 1024
        val scale = Math.min(maxDim.toFloat() / width, maxDim.toFloat() / height)
        val resized = if (scale < 1) Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true) else this
        resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
