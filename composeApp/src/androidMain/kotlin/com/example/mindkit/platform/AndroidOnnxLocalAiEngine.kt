package com.example.mindkit.platform

import ai.onnxruntime.genai.Generator
import ai.onnxruntime.genai.GeneratorParams
import ai.onnxruntime.genai.Model
import ai.onnxruntime.genai.Tokenizer
import com.example.mindkit.core.platform.LocalAiEngine
import com.example.mindkit.feature.chat.domain.AiChatRequest
import com.example.mindkit.feature.chat.domain.AiGenerationConfig
import com.example.mindkit.feature.chat.domain.AiToken
import com.example.mindkit.feature.chat.domain.ChatRole
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.feature.modeldownload.domain.ModelRuntime
import com.example.mindkit.feature.modelsettings.domain.AiEngineInfo
import com.example.mindkit.feature.modelsettings.domain.AiEngineType
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AndroidOnnxLocalAiEngine : LocalAiEngine {
    private var model: Model? = null
    private var tokenizer: Tokenizer? = null
    private var chatTemplate: String? = null
    private val generationCancelled = AtomicBoolean(false)
    private var loadedModelName = "No model loaded"

    override suspend fun getEngineInfo(): AiEngineInfo = AiEngineInfo(
        type = AiEngineType.AndroidOnnx,
        displayName = "ONNX Runtime",
        modelName = loadedModelName,
        requiresModelDownload = true,
        isAvailable = isModelLoaded(),
        statusText = if (isModelLoaded()) {
            "Running locally with downloaded ONNX model"
        } else {
            "Model not loaded"
        },
    )

    override suspend fun isAvailable(): Boolean = isModelLoaded()

    override suspend fun load(
        manifest: LocalModelManifest,
        modelDirectoryPath: String?,
    ): Result<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            require(manifest.runtime == ModelRuntime.Onnx) {
                "Android engine requires an ONNX model"
            }
            require(!modelDirectoryPath.isNullOrBlank()) { "ONNX model directory is required" }
            val modelDirectory = File(modelDirectoryPath)
            require(File(modelDirectory, "genai_config.json").isFile) {
                "The model bundle must contain genai_config.json"
            }

            unloadCurrentModel()
            val loadedModel = Model(modelDirectory.absolutePath)
            val loadedTokenizer = try {
                Tokenizer(loadedModel)
            } catch (error: Throwable) {
                loadedModel.close()
                throw error
            }

            model = loadedModel
            tokenizer = loadedTokenizer
            chatTemplate = readChatTemplate(modelDirectory)
            loadedModelName = manifest.displayName
        }
    }

    override fun generate(request: AiChatRequest, config: AiGenerationConfig): Flow<AiToken> = flow {
        val activeModel = model
        val activeTokenizer = tokenizer
        if (activeModel == null || activeTokenizer == null) {
            emit(AiToken.Failed("Model is not loaded"))
            return@flow
        }

        generationCancelled.set(false)
        try {
            val formattedPrompt = applyChatTemplate(activeTokenizer, request)
            activeTokenizer.encode(formattedPrompt).use { inputSequences ->
                val promptLength = inputSequences.getSequence(0).size
                GeneratorParams(activeModel).use { params ->
                    applyGenerationConfig(params, config, promptLength)
                    Generator(activeModel, params).use { generator ->
                        generator.appendTokenSequences(inputSequences)
                        streamTokens(generator, activeTokenizer, config.stopSequences)
                    }
                }
            }

            if (generationCancelled.get()) {
                emit(AiToken.Failed("Generation cancelled"))
            } else {
                emit(AiToken.Completed)
            }
        } catch (error: Throwable) {
            emit(AiToken.Failed(error.message ?: "ONNX generation failed"))
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun cancelGeneration() {
        generationCancelled.set(true)
    }

    private fun applyGenerationConfig(
        params: GeneratorParams,
        config: AiGenerationConfig,
        promptLength: Int,
    ) {
        params.setSearchOption("max_length", (promptLength + config.maxNewTokens).toDouble())
        params.setSearchOption("do_sample", config.temperature > 0f)
        if (config.temperature > 0f) {
            params.setSearchOption("temperature", config.temperature.toDouble())
            params.setSearchOption("top_p", config.topP.toDouble())
            params.setSearchOption("top_k", config.topK.toDouble())
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<AiToken>.streamTokens(
        generator: Generator,
        tokenizer: Tokenizer,
        stopSequences: List<String>,
    ) {
        val activeStops = stopSequences.filter { it.isNotEmpty() }
        val retainedCharacters = (activeStops.maxOfOrNull { it.length } ?: 1) - 1
        val pending = StringBuilder()

        tokenizer.createStream().use { tokenStream ->
            while (!generator.isDone && !generationCancelled.get()) {
                generator.generateNextToken()
                val text = tokenStream.decode(generator.getLastTokenInSequence(0))
                if (text.isEmpty()) continue

                pending.append(text)
                val stopIndex = activeStops
                    .map { pending.indexOf(it) }
                    .filter { it >= 0 }
                    .minOrNull()
                if (stopIndex != null) {
                    if (stopIndex > 0) emit(AiToken.Text(pending.substring(0, stopIndex)))
                    return
                }

                val emitLength = pending.length - retainedCharacters
                if (emitLength > 0) {
                    emit(AiToken.Text(pending.substring(0, emitLength)))
                    pending.delete(0, emitLength)
                }
            }
        }

        if (pending.isNotEmpty() && !generationCancelled.get()) {
            emit(AiToken.Text(pending.toString()))
        }
    }

    private fun isModelLoaded(): Boolean = model != null && tokenizer != null

    private fun readChatTemplate(modelDirectory: File): String? {
        val tokenizerConfig = File(modelDirectory, "tokenizer_config.json")
        if (!tokenizerConfig.isFile) return null
        return JSONObject(tokenizerConfig.readText())
            .optString("chat_template")
            .takeIf { it.isNotBlank() }
    }

    private fun applyChatTemplate(tokenizer: Tokenizer, request: AiChatRequest): String {
        val template = chatTemplate ?: return request.messages.last().content
        val messages = JSONArray()
            .put(
                JSONObject()
                    .put("role", "system")
                    .put("content", request.systemInstruction),
            )
        request.messages.forEach { message ->
            messages.put(
                JSONObject()
                    .put(
                        "role",
                        if (message.role == ChatRole.User) "user" else "assistant",
                    )
                    .put("content", message.content),
            )
        }
        return tokenizer.applyChatTemplate(template, messages.toString(), null, true)
    }

    private fun unloadCurrentModel() {
        tokenizer?.close()
        model?.close()
        tokenizer = null
        model = null
        chatTemplate = null
    }
}
