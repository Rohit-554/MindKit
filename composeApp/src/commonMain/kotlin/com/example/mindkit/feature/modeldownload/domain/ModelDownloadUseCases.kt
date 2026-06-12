package com.example.mindkit.feature.modeldownload.domain

import com.example.mindkit.feature.chat.data.LocalAiRepository
import com.example.mindkit.feature.modeldownload.data.ModelDownloadRepository
import kotlinx.coroutines.flow.Flow

class DownloadModelUseCase(
    private val repository: ModelDownloadRepository,
) {
    operator fun invoke(): Flow<ModelDownloadState> = repository.downloadModel()
}

class CheckModelAvailabilityUseCase(
    private val repository: ModelDownloadRepository,
) {
    suspend operator fun invoke(): Boolean = repository.isModelReady()
}

class InitializeLocalAiUseCase(
    private val manifest: LocalModelManifest,
    private val modelDownloadRepository: ModelDownloadRepository,
    private val localAiRepository: LocalAiRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        if (!modelDownloadRepository.isModelReady()) {
            return Result.failure(IllegalStateException("Model is not ready"))
        }
        return localAiRepository.load(
            manifest = manifest,
            modelDirectoryPath = modelDownloadRepository.getModelDirectoryPath(),
        )
    }
}
