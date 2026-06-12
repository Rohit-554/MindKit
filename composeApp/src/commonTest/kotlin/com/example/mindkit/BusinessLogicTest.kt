package com.example.mindkit

import com.example.mindkit.core.platform.ChecksumValidator
import com.example.mindkit.core.platform.LocalAiEngine
import com.example.mindkit.core.platform.ModelDownloadProgress
import com.example.mindkit.core.platform.ModelFileStorage
import com.example.mindkit.core.platform.ZipExtractProgress
import com.example.mindkit.core.platform.ZipExtractor
import com.example.mindkit.core.platform.ZipModelDownloader
import com.example.mindkit.feature.chat.data.LocalAiRepository
import com.example.mindkit.feature.chat.domain.AiGenerationConfig
import com.example.mindkit.feature.chat.domain.AiTaskMode
import com.example.mindkit.feature.chat.domain.AiToken
import com.example.mindkit.feature.chat.domain.PromptBuilder
import com.example.mindkit.feature.modeldownload.data.ModelDownloadRepository
import com.example.mindkit.feature.modeldownload.domain.DefaultModels
import com.example.mindkit.feature.modeldownload.domain.InitializeLocalAiUseCase
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.feature.modeldownload.domain.ModelDownloadState
import com.example.mindkit.feature.modelsettings.domain.AiEngineInfo
import com.example.mindkit.feature.modelsettings.domain.AiEngineType
import com.example.mindkit.feature.modelsettings.domain.ModelState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BusinessLogicTest {
    @Test
    fun promptBuilderAppliesSelectedModeAndTrimsInput() {
        val prompt = PromptBuilder().buildPrompt(
            mode = AiTaskMode.Summarize,
            userInput = "  A long document  ",
        )

        assertTrue(prompt.startsWith("Summarize the following text"))
        assertTrue(prompt.endsWith("A long document"))
    }

    @Test
    fun zipBundleDownloadEmitsOrderedLifecycleAndReadyPath() = runTest {
        val storage = FakeModelFileStorage()
        val repository = ModelDownloadRepository(
            manifest = DefaultModels.AndroidGemma3_270M_Onnx,
            zipModelDownloader = FakeDownloader(),
            zipExtractor = FakeExtractor(),
            modelFileStorage = storage,
            checksumValidator = FakeChecksumValidator(),
        )

        val states = repository.downloadModel().toList()

        assertIs<ModelDownloadState.DownloadingZip>(states[0])
        assertIs<ModelDownloadState.DownloadingZip>(states[1])
        assertIs<ModelDownloadState.VerifyingZip>(states[2])
        assertIs<ModelDownloadState.ExtractingZip>(states[3])
        assertIs<ModelDownloadState.VerifyingExtractedFiles>(states[4])
        assertEquals(ModelDownloadState.Ready("/models/test"), states[5])
        assertTrue(storage.tempCleared)
    }

    @Test
    fun initializeLocalAiLoadsReadySystemModelAndSharesReadyState() = runTest {
        val engine = FakeLocalAiEngine()
        val localAiRepository = LocalAiRepository(engine)
        val manifest = DefaultModels.IosAppleFoundationModels
        val downloadRepository = ModelDownloadRepository(manifest = manifest)
        val initialize = InitializeLocalAiUseCase(
            manifest = manifest,
            modelDownloadRepository = downloadRepository,
            localAiRepository = localAiRepository,
        )

        val result = initialize()

        assertTrue(result.isSuccess)
        assertEquals(ModelState.Ready, localAiRepository.modelState.value)
        assertEquals(manifest, engine.loadedManifest)
    }
}

private class FakeDownloader : ZipModelDownloader {
    override fun downloadZip(
        url: String,
        destinationZipPath: String,
    ): Flow<ModelDownloadProgress> = flowOf(
        ModelDownloadProgress(downloadedBytes = 5, totalBytes = 10),
        ModelDownloadProgress(downloadedBytes = 10, totalBytes = 10),
    )
}

private class FakeExtractor : ZipExtractor {
    override fun extract(
        zipPath: String,
        destinationDirectoryPath: String,
    ): Flow<ZipExtractProgress> = flowOf(
        ZipExtractProgress(extractedFiles = 1, totalFiles = 1),
    )
}

private class FakeChecksumValidator : ChecksumValidator {
    override suspend fun validateSha256(filePath: String, expectedSha256: String?): Boolean = true
}

private class FakeModelFileStorage : ModelFileStorage {
    var ready = false
    var tempCleared = false

    override suspend fun getFinalModelDirectoryPath(modelId: String): String = "/models/test"
    override suspend fun getTempZipPath(modelId: String, zipFileName: String): String = "/tmp/model.zip"
    override suspend fun getTempExtractDirectoryPath(modelId: String): String = "/tmp/extracted"
    override suspend fun isModelReady(modelId: String, requiredFiles: List<String>): Boolean = ready
    override suspend fun deleteModel(modelId: String): Result<Unit> = Result.success(Unit)

    override suspend fun clearTemp(modelId: String): Result<Unit> {
        tempCleared = true
        return Result.success(Unit)
    }

    override suspend fun moveTempExtractToFinal(modelId: String): Result<Unit> {
        ready = true
        return Result.success(Unit)
    }
}

private class FakeLocalAiEngine : LocalAiEngine {
    var loadedManifest: LocalModelManifest? = null

    override suspend fun getEngineInfo(): AiEngineInfo = AiEngineInfo(
        type = AiEngineType.AppleFoundationModels,
        displayName = "Fake",
        modelName = "Fake",
        requiresModelDownload = false,
        isAvailable = loadedManifest != null,
        statusText = "Fake",
    )

    override suspend fun isAvailable(): Boolean = loadedManifest != null

    override suspend fun load(
        manifest: LocalModelManifest,
        modelDirectoryPath: String?,
    ): Result<Unit> {
        loadedManifest = manifest
        return Result.success(Unit)
    }

    override fun generate(prompt: String, config: AiGenerationConfig): Flow<AiToken> =
        flowOf(AiToken.Completed)

    override suspend fun cancelGeneration() = Unit
}
