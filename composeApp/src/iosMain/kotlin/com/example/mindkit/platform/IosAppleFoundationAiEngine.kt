package com.example.mindkit.platform

import com.example.mindkit.core.platform.DeviceCapabilityChecker
import com.example.mindkit.core.platform.LocalAiEngine
import com.example.mindkit.feature.chat.domain.AiGenerationConfig
import com.example.mindkit.feature.chat.domain.AiToken
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.feature.modeldownload.domain.ModelRuntime
import com.example.mindkit.feature.modelsettings.domain.AiEngineInfo
import com.example.mindkit.feature.modelsettings.domain.AiEngineType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class IosAppleFoundationAiEngine(
    private val capabilityChecker: DeviceCapabilityChecker,
) : LocalAiEngine {
    private var loaded = false
    private var statusText = "Apple Foundation Models not initialized"

    override suspend fun getEngineInfo(): AiEngineInfo {
        refreshAvailability()
        return AiEngineInfo(
            type = AiEngineType.AppleFoundationModels,
            displayName = "Apple Foundation Models",
            modelName = "Apple on-device language model",
            requiresModelDownload = false,
            isAvailable = loaded,
            statusText = statusText,
        )
    }

    override suspend fun isAvailable(): Boolean {
        refreshAvailability()
        return loaded
    }

    override suspend fun load(
        manifest: LocalModelManifest,
        modelDirectoryPath: String?,
    ): Result<Unit> = runCatching {
        require(manifest.runtime == ModelRuntime.AppleFoundationModels) {
            "iOS engine requires Apple Foundation Models"
        }
        refreshAvailability()
        check(loaded) { statusText }
    }

    override fun generate(prompt: String, config: AiGenerationConfig): Flow<AiToken> = callbackFlow {
        if (!loaded) {
            trySend(AiToken.Failed(statusText))
            close()
            return@callbackFlow
        }

        requireAppleFoundationModelsBridge().generate(
            prompt = prompt,
            maxTokens = config.maxNewTokens,
            temperature = config.temperature.toDouble(),
            topP = config.topP.toDouble(),
            callback = object : AppleModelGenerationCallback {
                override fun onText(text: String) {
                    trySend(AiToken.Text(text))
                }

                override fun onComplete() {
                    trySend(AiToken.Completed)
                    close()
                }

                override fun onError(message: String) {
                    trySend(AiToken.Failed(message))
                    close()
                }
            },
        )

        awaitClose { requireAppleFoundationModelsBridge().cancel() }
    }

    override suspend fun cancelGeneration() {
        requireAppleFoundationModelsBridge().cancel()
    }

    private suspend fun refreshAvailability() {
        suspendCancellableCoroutine { continuation ->
            requireAppleFoundationModelsBridge().checkAvailability(
                object : AppleModelAvailabilityCallback {
                    override fun onResult(available: Boolean, statusText: String) {
                        loaded = available
                        this@IosAppleFoundationAiEngine.statusText = statusText
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                }
            )
        }
    }
}
