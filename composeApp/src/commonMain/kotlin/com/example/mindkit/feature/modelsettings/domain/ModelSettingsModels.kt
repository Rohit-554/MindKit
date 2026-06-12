package com.example.mindkit.feature.modelsettings.domain

import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest

enum class AiEngineType {
    AndroidOnnx,
    AppleFoundationModels,
    Unsupported,
}

data class AiEngineInfo(
    val type: AiEngineType,
    val displayName: String,
    val modelName: String,
    val requiresModelDownload: Boolean,
    val isAvailable: Boolean,
    val statusText: String,
)

sealed interface ModelState {
    data object NotDownloaded : ModelState
    data object Unsupported : ModelState
    data object Downloading : ModelState
    data object Verifying : ModelState
    data object Extracting : ModelState
    data object Loading : ModelState
    data object Ready : ModelState
    data class Failed(val reason: String) : ModelState
}

data class ModelInfo(
    val manifest: LocalModelManifest,
    val engineInfo: AiEngineInfo,
    val storageUsed: String? = null,
    val ramUsage: String? = null,
    val loadedAt: String? = null,
)
