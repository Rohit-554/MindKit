package com.example.mindkit.core.platform

import kotlinx.coroutines.flow.Flow

data class ModelDownloadProgress(
    val downloadedBytes: Long,
    val totalBytes: Long?,
) {
    val progress: Float?
        get() = totalBytes
            ?.takeIf { it > 0L }
            ?.let { (downloadedBytes.toDouble() / it).toFloat().coerceIn(0f, 1f) }
}

data class ZipExtractProgress(
    val extractedFiles: Int,
    val totalFiles: Int?,
) {
    val progress: Float?
        get() = totalFiles
            ?.takeIf { it > 0 }
            ?.let { (extractedFiles.toFloat() / it).coerceIn(0f, 1f) }
}

interface ZipModelDownloader {
    fun downloadZip(url: String, destinationZipPath: String): Flow<ModelDownloadProgress>
}

interface ZipExtractor {
    fun extract(zipPath: String, destinationDirectoryPath: String): Flow<ZipExtractProgress>
}

interface ModelFileStorage {
    suspend fun getFinalModelDirectoryPath(modelId: String): String
    suspend fun getTempZipPath(modelId: String, zipFileName: String): String
    suspend fun getTempExtractDirectoryPath(modelId: String): String
    suspend fun isModelReady(modelId: String, requiredFiles: List<String>): Boolean
    suspend fun deleteModel(modelId: String): Result<Unit>
    suspend fun clearTemp(modelId: String): Result<Unit>
    suspend fun moveTempExtractToFinal(modelId: String): Result<Unit>
}

interface ChecksumValidator {
    suspend fun validateSha256(filePath: String, expectedSha256: String?): Boolean
}

interface DeviceCapabilityChecker {
    suspend fun isLocalAiSupported(): Boolean
}
