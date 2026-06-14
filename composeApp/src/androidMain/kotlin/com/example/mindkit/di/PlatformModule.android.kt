package com.example.mindkit.di

import com.example.mindkit.core.platform.ChecksumValidator
import com.example.mindkit.core.platform.DeviceCapabilityChecker
import com.example.mindkit.core.platform.LocalAiEngine
import com.example.mindkit.core.platform.ModelFileStorage
import com.example.mindkit.core.platform.ZipExtractor
import com.example.mindkit.core.platform.ZipModelDownloader
import com.example.mindkit.feature.modeldownload.data.ModelDownloadRepository
import com.example.mindkit.feature.modeldownload.domain.DefaultModels
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.notifications.NotificationScheduler
import com.example.mindkit.platform.AndroidChecksumValidator
import com.example.mindkit.platform.AndroidDeviceCapabilityChecker
import com.example.mindkit.platform.AndroidModelFileStorage
import com.example.mindkit.platform.AndroidModelManifestProvider
import com.example.mindkit.platform.AndroidOnnxLocalAiEngine
import com.example.mindkit.platform.AndroidZipExtractor
import com.example.mindkit.platform.AndroidZipModelDownloader
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { NotificationScheduler(androidContext()) }
    single { AndroidModelManifestProvider(androidContext()) }
    single<LocalModelManifest> { get<AndroidModelManifestProvider>().resolveManifest() }
    single<LocalAiEngine> { AndroidOnnxLocalAiEngine() }
    single<DeviceCapabilityChecker> { AndroidDeviceCapabilityChecker() }
    single<ZipModelDownloader> { AndroidZipModelDownloader() }
    single<ZipExtractor> { AndroidZipExtractor() }
    single<ModelFileStorage> { AndroidModelFileStorage(androidContext()) }
    single<ChecksumValidator> { AndroidChecksumValidator() }
    single {
        ModelDownloadRepository(
            manifest = get(),
            zipModelDownloader = get(),
            zipExtractor = get(),
            modelFileStorage = get(),
            checksumValidator = get(),
        )
    }
}
