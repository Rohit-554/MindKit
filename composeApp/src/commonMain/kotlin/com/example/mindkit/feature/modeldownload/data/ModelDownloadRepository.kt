package com.example.mindkit.feature.modeldownload.data

import com.example.mindkit.core.platform.ChecksumValidator
import com.example.mindkit.core.platform.ModelFileStorage
import com.example.mindkit.core.platform.ZipExtractor
import com.example.mindkit.core.platform.ZipModelDownloader
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.feature.modeldownload.domain.ModelDelivery
import com.example.mindkit.feature.modeldownload.domain.ModelDownloadState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class ModelDownloadRepository(
    private val manifest: LocalModelManifest,
    private val zipModelDownloader: ZipModelDownloader? = null,
    private val zipExtractor: ZipExtractor? = null,
    private val modelFileStorage: ModelFileStorage? = null,
    private val checksumValidator: ChecksumValidator? = null,
) {
    suspend fun isModelReady(): Boolean = when (manifest.delivery) {
        ModelDelivery.SystemProvided -> true
        is ModelDelivery.ZipBundle -> storage().isModelReady(
            modelId = manifest.id,
            requiredFiles = manifest.requiredFiles,
        )
    }

    suspend fun getModelDirectoryPath(): String? = when (manifest.delivery) {
        ModelDelivery.SystemProvided -> null
        is ModelDelivery.ZipBundle -> storage().getFinalModelDirectoryPath(manifest.id)
    }

    fun downloadModel(): Flow<ModelDownloadState> = flow {
        if (manifest.delivery is ModelDelivery.SystemProvided) {
            emit(ModelDownloadState.Ready(modelDirectoryPath = null))
            return@flow
        }
        val delivery = manifest.delivery as ModelDelivery.ZipBundle
        val storage = storage()
        emit(ModelDownloadState.DownloadingZip(null))
        storage.clearTemp(manifest.id).getOrThrow()
        val tempZipPath = storage.getTempZipPath(manifest.id, delivery.fileName)

        emitAll(downloadZipBundle(delivery, tempZipPath))

        emit(ModelDownloadState.VerifyingZip)
        if (!verifyZipChecksum(tempZipPath, delivery)) {
            storage.clearTemp(manifest.id)
            emit(ModelDownloadState.Failed("Zip verification failed"))
            return@flow
        }

        val tempExtractPath = storage.getTempExtractDirectoryPath(manifest.id)
        emitAll(extractZipBundle(tempZipPath, tempExtractPath))

        emit(ModelDownloadState.VerifyingExtractedFiles)
        emitAll(finalizeModelInstallation(storage))
    }.catch { error ->
        modelFileStorage?.clearTemp(manifest.id)
        if (error is CancellationException) throw error
        emit(ModelDownloadState.Failed(error.message ?: "Model download failed"))
    }

    private fun downloadZipBundle(
        delivery: ModelDelivery.ZipBundle,
        tempZipPath: String,
    ): Flow<ModelDownloadState> = flow {
        val downloader = requireNotNull(zipModelDownloader) {
            "ZipModelDownloader is required for ZipBundle delivery"
        }
        downloader.downloadZip(delivery.downloadUrl, tempZipPath).collect { progress ->
            delivery.expectedSizeBytes?.let { expectedSize ->
                if (progress.downloadedBytes > expectedSize) {
                    error("Downloaded model exceeds the configured expected size")
                }
            }
            emit(ModelDownloadState.DownloadingZip(
                progress = progress.progress,
                downloadedBytes = progress.downloadedBytes,
                totalBytes = progress.totalBytes,
            ))
        }
    }

    private suspend fun verifyZipChecksum(
        tempZipPath: String,
        delivery: ModelDelivery.ZipBundle,
    ): Boolean {
        val validator = requireNotNull(checksumValidator) {
            "ChecksumValidator is required for ZipBundle delivery"
        }
        return validator.validateSha256(tempZipPath, delivery.checksumSha256)
    }

    private fun extractZipBundle(
        tempZipPath: String,
        tempExtractPath: String,
    ): Flow<ModelDownloadState> = flow {
        val extractor = requireNotNull(zipExtractor) {
            "ZipExtractor is required for ZipBundle delivery"
        }
        extractor.extract(tempZipPath, tempExtractPath).collect { progress ->
            emit(ModelDownloadState.ExtractingZip(progress.progress))
        }
    }

    private fun finalizeModelInstallation(storage: ModelFileStorage): Flow<ModelDownloadState> = flow {
        storage.moveTempExtractToFinal(manifest.id).getOrThrow()
        if (storage.isModelReady(manifest.id, manifest.requiredFiles)) {
            storage.clearTemp(manifest.id)
            emit(ModelDownloadState.Ready(storage.getFinalModelDirectoryPath(manifest.id)))
        } else {
            storage.deleteModel(manifest.id)
            storage.clearTemp(manifest.id)
            emit(ModelDownloadState.Failed("Required model files are missing after extraction"))
        }
    }

    suspend fun deleteModel(): Result<Unit> = when (manifest.delivery) {
        ModelDelivery.SystemProvided -> Result.success(Unit)
        is ModelDelivery.ZipBundle -> storage().deleteModel(manifest.id)
    }

    private fun storage(): ModelFileStorage = requireNotNull(modelFileStorage) {
        "ModelFileStorage is required for ZipBundle delivery"
    }
}
