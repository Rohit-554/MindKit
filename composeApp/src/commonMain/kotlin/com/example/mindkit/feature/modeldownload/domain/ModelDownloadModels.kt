package com.example.mindkit.feature.modeldownload.domain

import com.example.mindkit.feature.chat.domain.AiGenerationConfig

enum class ModelRuntime {
    Onnx,
    AppleFoundationModels,
}

sealed interface ModelDelivery {
    data class ZipBundle(
        val fileName: String,
        val downloadUrl: String,
        val expectedSizeBytes: Long?,
        val checksumSha256: String?,
    ) : ModelDelivery

    data object SystemProvided : ModelDelivery
}

data class LocalModelManifest(
    val id: String,
    val displayName: String,
    val provider: String,
    val version: String,
    val runtime: ModelRuntime,
    val delivery: ModelDelivery,
    val entryFileName: String,
    val tokenizerFileName: String?,
    val requiredFiles: List<String>,
    val expectedExtractedSizeBytes: Long?,
    val generationDefaults: AiGenerationConfig,
)

sealed interface ModelDownloadState {
    data object NotDownloaded : ModelDownloadState
    data class DownloadingZip(
        val progress: Float?,
        val downloadedBytes: Long = 0L,
        val totalBytes: Long? = null,
    ) : ModelDownloadState
    data object VerifyingZip : ModelDownloadState
    data class ExtractingZip(val progress: Float?) : ModelDownloadState
    data object VerifyingExtractedFiles : ModelDownloadState
    data class Ready(val modelDirectoryPath: String?) : ModelDownloadState
    data class Failed(val reason: String) : ModelDownloadState
}

object DefaultModels {
    val AndroidGemma3_270M_Onnx = LocalModelManifest(
        id = "google-gemma-3-270m-onnx",
        displayName = "Gemma 3 270M",
        provider = "Google",
        version = "1.0",
        runtime = ModelRuntime.Onnx,
        delivery = ModelDelivery.ZipBundle(
            fileName = "gemma-3-270m-onnx.zip",
            downloadUrl = "https://github.com/Rohit-554/MindKit/releases/download/gemma-3-270m-onnx-v1/gemma-3-270m-onnx.zip",
            expectedSizeBytes = 251500897,
            checksumSha256 = "525a3c9f30208638d9bbb40f85e18d345779b2d46760892497f8da63a08d094d",
        ),
        entryFileName = "genai_config.json",
        tokenizerFileName = "tokenizer.json",
        requiredFiles = listOf(
            "genai_config.json",
            "tokenizer.json",
            "tokenizer_config.json",
        ),
        expectedExtractedSizeBytes = null,
        generationDefaults = AiGenerationConfig(),
    )

    val IosAppleFoundationModels = LocalModelManifest(
        id = "apple-foundation-models",
        displayName = "Apple Foundation Models",
        provider = "Apple",
        version = "system",
        runtime = ModelRuntime.AppleFoundationModels,
        delivery = ModelDelivery.SystemProvided,
        entryFileName = "",
        tokenizerFileName = null,
        requiredFiles = emptyList(),
        expectedExtractedSizeBytes = null,
        generationDefaults = AiGenerationConfig(),
    )
}
