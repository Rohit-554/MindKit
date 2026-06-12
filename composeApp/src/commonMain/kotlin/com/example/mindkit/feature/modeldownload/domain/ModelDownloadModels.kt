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
    data class DownloadingZip(val progress: Float?) : ModelDownloadState
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
            downloadUrl = "https://YOUR_CDN_URL/models/gemma-3-270m/gemma-3-270m-onnx.zip",
            expectedSizeBytes = null,
            checksumSha256 = null,
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
