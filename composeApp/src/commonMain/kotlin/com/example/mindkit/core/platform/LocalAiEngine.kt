package com.example.mindkit.core.platform

import com.example.mindkit.feature.chat.domain.AiGenerationConfig
import com.example.mindkit.feature.chat.domain.AiChatRequest
import com.example.mindkit.feature.chat.domain.AiToken
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.feature.modelsettings.domain.AiEngineInfo
import kotlinx.coroutines.flow.Flow

interface LocalAiEngine {
    suspend fun getEngineInfo(): AiEngineInfo
    suspend fun isAvailable(): Boolean
    suspend fun load(
        manifest: LocalModelManifest,
        modelDirectoryPath: String?,
    ): Result<Unit>

    fun generate(request: AiChatRequest, config: AiGenerationConfig): Flow<AiToken>
    suspend fun cancelGeneration()
}
