package com.example.mindkit.platform

import com.example.mindkit.core.platform.LocalAiEngine
import com.example.mindkit.feature.chat.domain.AiChatRequest
import com.example.mindkit.feature.chat.domain.AiGenerationConfig
import com.example.mindkit.feature.chat.domain.AiToken
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.feature.modelsettings.domain.AiEngineInfo
import com.example.mindkit.feature.modelsettings.domain.AiEngineType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DesktopLocalAiEngine : LocalAiEngine {
    override suspend fun getEngineInfo(): AiEngineInfo = AiEngineInfo(
        type = AiEngineType.Unsupported,
        displayName = "Unsupported",
        modelName = "None",
        requiresModelDownload = false,
        isAvailable = false,
        statusText = "Local AI is not configured for desktop",
    )

    override suspend fun isAvailable(): Boolean = false

    override suspend fun load(
        manifest: LocalModelManifest,
        modelDirectoryPath: String?,
    ): Result<Unit> = Result.failure(UnsupportedOperationException("Desktop local AI is unsupported"))

    override fun generate(request: AiChatRequest, config: AiGenerationConfig): Flow<AiToken> =
        flowOf(AiToken.Failed("Desktop local AI is unsupported"))

    override suspend fun cancelGeneration() = Unit
}
