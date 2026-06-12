package com.example.mindkit.platform

import android.content.Context
import android.content.pm.PackageManager
import com.example.mindkit.feature.modeldownload.domain.DefaultModels
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.feature.modeldownload.domain.ModelDelivery

class AndroidModelManifestProvider(
    private val context: Context,
) {
    fun resolveManifest(): LocalModelManifest {
        val applicationInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA,
        )
        val metadata = applicationInfo.metaData
        val defaults = DefaultModels.AndroidGemma3_270M_Onnx
        val defaultDelivery = defaults.delivery as ModelDelivery.ZipBundle

        return defaults.copy(
            delivery = defaultDelivery.copy(
                downloadUrl = metadata
                    ?.getString(MODEL_DOWNLOAD_URL)
                    ?.takeIf { it.isNotBlank() }
                    ?: defaultDelivery.downloadUrl,
                checksumSha256 = metadata
                    ?.getString(MODEL_CHECKSUM_SHA256)
                    ?.takeIf { it.isNotBlank() },
                expectedSizeBytes = metadata
                    ?.getString(MODEL_EXPECTED_SIZE_BYTES)
                    ?.toLongOrNull(),
            ),
        )
    }

    private companion object {
        const val MODEL_DOWNLOAD_URL = "com.example.mindkit.MODEL_DOWNLOAD_URL"
        const val MODEL_CHECKSUM_SHA256 = "com.example.mindkit.MODEL_CHECKSUM_SHA256"
        const val MODEL_EXPECTED_SIZE_BYTES = "com.example.mindkit.MODEL_EXPECTED_SIZE_BYTES"
    }
}
