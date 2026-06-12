package com.example.mindkit.platform

import android.content.Context
import com.example.mindkit.core.platform.ChecksumValidator
import com.example.mindkit.core.platform.DeviceCapabilityChecker
import com.example.mindkit.core.platform.ModelDownloadProgress
import com.example.mindkit.core.platform.ModelFileStorage
import com.example.mindkit.core.platform.ZipExtractProgress
import com.example.mindkit.core.platform.ZipExtractor
import com.example.mindkit.core.platform.ZipModelDownloader
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AndroidZipModelDownloader(
    private val client: HttpClient,
) : ZipModelDownloader {
    override fun downloadZip(
        url: String,
        destinationZipPath: String,
    ): Flow<ModelDownloadProgress> = flow {
        require(!url.contains("YOUR_CDN_URL")) { "Configure a real model download URL" }

        val response: HttpResponse = client.get(url)
        require(response.status.value in 200..299) {
            "Model download failed with HTTP ${response.status.value}"
        }
        val totalBytes = response.contentLength()
        require(totalBytes == null || totalBytes > 0L) {
            "Model download returned an empty response"
        }
        val destination = File(destinationZipPath).apply { parentFile?.mkdirs() }

        emitAll(writeChunksToFile(response.body(), destination, totalBytes))
    }.flowOn(Dispatchers.IO)

    private fun writeChunksToFile(
        channel: ByteReadChannel,
        destination: File,
        totalBytes: Long?,
    ): Flow<ModelDownloadProgress> = flow {
        FileOutputStream(destination).use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var downloaded = 0L
            while (true) {
                val count = channel.readAvailable(buffer)
                if (count < 0) break
                if (count == 0) continue
                output.write(buffer, 0, count)
                downloaded += count
                emit(ModelDownloadProgress(downloaded, totalBytes))
            }
            require(downloaded > 0L) { "Downloaded model ZIP is empty" }
        }
    }
}

class AndroidZipExtractor : ZipExtractor {
    override fun extract(
        zipPath: String,
        destinationDirectoryPath: String,
    ): Flow<ZipExtractProgress> = flow {
        val zipFile = File(zipPath)
        val destination = File(destinationDirectoryPath).apply {
            deleteRecursively()
            mkdirs()
        }
        val destinationRoot = destination.canonicalFile
        val totalFiles = countZipFiles(zipFile)

        var extractedFiles = 0
        ZipInputStream(FileInputStream(zipFile)).use { input ->
            var entry = input.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    extractZipEntry(input, entry.name, destination, destinationRoot)
                    extractedFiles += 1
                    emit(ZipExtractProgress(extractedFiles, totalFiles))
                } else {
                    File(destination, entry.name).canonicalFile.mkdirs()
                }
                input.closeEntry()
                entry = input.nextEntry
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun countZipFiles(zipFile: File): Int =
        ZipFile(zipFile).use { archive ->
            archive.entries().asSequence().count { !it.isDirectory }
        }

    private fun extractZipEntry(
        input: ZipInputStream,
        entryName: String,
        destination: File,
        destinationRoot: File,
    ) {
        val outputFile = File(destination, entryName).canonicalFile
        require(
            outputFile.path == destinationRoot.path ||
                outputFile.path.startsWith(destinationRoot.path + File.separator),
        ) { "Unsafe zip entry: $entryName" }
        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { output -> input.copyTo(output) }
    }
}

class AndroidModelFileStorage(
    context: Context,
) : ModelFileStorage {
    private val modelsRoot = File(context.filesDir, "models")
    private val tempRoot = File(context.cacheDir, "model-downloads")

    override suspend fun getFinalModelDirectoryPath(modelId: String): String =
        File(modelsRoot, modelId).absolutePath

    override suspend fun getTempZipPath(modelId: String, zipFileName: String): String =
        File(tempDirectory(modelId), zipFileName).absolutePath

    override suspend fun getTempExtractDirectoryPath(modelId: String): String =
        File(tempDirectory(modelId), "extracted").absolutePath

    override suspend fun isModelReady(modelId: String, requiredFiles: List<String>): Boolean {
        val modelDirectory = File(modelsRoot, modelId)
        return modelDirectory.isDirectory &&
            requiredFiles.all { File(modelDirectory, it).isFile }
    }

    override suspend fun deleteModel(modelId: String): Result<Unit> = runCatching {
        check(File(modelsRoot, modelId).deleteRecursively()) { "Unable to delete model files" }
    }

    override suspend fun clearTemp(modelId: String): Result<Unit> = runCatching {
        val directory = File(tempRoot, modelId)
        if (directory.exists()) {
            check(directory.deleteRecursively()) { "Unable to clear temporary model files" }
        }
    }

    override suspend fun moveTempExtractToFinal(modelId: String): Result<Unit> = runCatching {
        val extracted = File(tempDirectory(modelId), "extracted")
        require(extracted.isDirectory) { "Temporary extraction directory does not exist" }

        val source = extracted.listFiles()
            ?.singleOrNull()
            ?.takeIf { it.isDirectory }
            ?: extracted
        val destination = File(modelsRoot, modelId)
        destination.deleteRecursively()
        destination.parentFile?.mkdirs()
        check(source.copyRecursively(destination, overwrite = true)) {
            "Unable to move extracted model files"
        }
    }

    private fun tempDirectory(modelId: String): File =
        File(tempRoot, modelId).apply { mkdirs() }
}

class AndroidChecksumValidator : ChecksumValidator {
    override suspend fun validateSha256(
        filePath: String,
        expectedSha256: String?,
    ): Boolean {
        if (expectedSha256.isNullOrBlank()) return true
        val actual = computeFileSha256(filePath)
        return actual.equals(expectedSha256.trim(), ignoreCase = true)
    }

    private fun computeFileSha256(filePath: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(filePath).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var count = input.read(buffer)
            while (count >= 0) {
                if (count > 0) digest.update(buffer, 0, count)
                count = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    }
}

class AndroidDeviceCapabilityChecker : DeviceCapabilityChecker {
    override suspend fun isLocalAiSupported(): Boolean = true
}
