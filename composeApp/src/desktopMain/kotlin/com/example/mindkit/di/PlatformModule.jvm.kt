package com.example.mindkit.di

import com.example.mindkit.core.platform.LocalAiEngine
import com.example.mindkit.feature.modeldownload.data.ModelDownloadRepository
import com.example.mindkit.feature.modeldownload.domain.DefaultModels
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.notifications.NotificationScheduler
import com.example.mindkit.platform.DesktopLocalAiEngine
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { NotificationScheduler() }
    single<LocalModelManifest> { DefaultModels.IosAppleFoundationModels }
    single<LocalAiEngine> { DesktopLocalAiEngine() }
    single { ModelDownloadRepository(manifest = get()) }
}
