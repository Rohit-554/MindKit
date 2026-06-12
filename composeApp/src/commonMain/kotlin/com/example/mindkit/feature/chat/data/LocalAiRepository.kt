package com.example.mindkit.feature.chat.data

import com.example.mindkit.core.platform.LocalAiEngine
import com.example.mindkit.feature.chat.domain.AiGenerationConfig
import com.example.mindkit.feature.chat.domain.AiToken
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.feature.modelsettings.domain.AiEngineInfo
import com.example.mindkit.feature.modelsettings.domain.ModelState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalAiRepository(
    private val localAiEngine: LocalAiEngine,
) {
    private val _modelState = MutableStateFlow<ModelState>(ModelState.NotDownloaded)
    val modelState: StateFlow<ModelState> = _modelState.asStateFlow()

    suspend fun getEngineInfo(): AiEngineInfo = localAiEngine.getEngineInfo()

    suspend fun isAvailable(): Boolean = localAiEngine.isAvailable()

    suspend fun load(
        manifest: LocalModelManifest,
        modelDirectoryPath: String?,
    ): Result<Unit> {
        _modelState.value = ModelState.Loading
        return localAiEngine.load(manifest, modelDirectoryPath)
            .onSuccess { _modelState.value = ModelState.Ready }
            .onFailure { error ->
                _modelState.value = ModelState.Failed(
                    error.message ?: "Failed to load the local AI engine",
                )
            }
    }

    fun updateModelState(state: ModelState) {
        _modelState.value = state
    }

    fun generate(prompt: String, config: AiGenerationConfig): Flow<AiToken> =
        localAiEngine.generate(prompt, config)

    suspend fun cancelGeneration() = localAiEngine.cancelGeneration()
}
