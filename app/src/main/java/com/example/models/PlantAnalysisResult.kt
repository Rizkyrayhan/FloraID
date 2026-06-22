package com.example.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlantAnalysisResult(
    @Json(name = "isPlant") val isPlant: Boolean,
    @Json(name = "matchPercentage") val matchPercentage: Int?,
    @Json(name = "commonName") val commonName: String?,
    @Json(name = "latinName") val latinName: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "careGuide") val careGuide: String?,
    @Json(name = "origin") val origin: String?,
    @Json(name = "tags") val tags: List<String>?,
    @Json(name = "diseaseName") val diseaseName: String? = null,
    @Json(name = "hasDisease") val hasDisease: Boolean? = null,
    @Json(name = "treatment") val treatment: String? = null,
    @Json(name = "isHealthy") val isHealthy: Boolean? = null,
    @Json(name = "isDead") val isDead: Boolean? = null,
    @Json(name = "healthAssessment") val healthAssessment: String? = null,
    @Json(name = "healthSolution") val healthSolution: String? = null
)
