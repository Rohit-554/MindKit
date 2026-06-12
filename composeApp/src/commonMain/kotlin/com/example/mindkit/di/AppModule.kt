package com.example.mindkit.di

import com.example.mindkit.network.ApiService
import com.example.mindkit.network.createHttpClient
import com.example.mindkit.feature.chat.data.LocalAiRepository
import com.example.mindkit.feature.chat.domain.PromptBuilder
import com.example.mindkit.feature.chat.domain.SendPromptUseCase
import com.example.mindkit.feature.chat.presentation.ChatViewModel
import com.example.mindkit.feature.modeldownload.domain.CheckModelAvailabilityUseCase
import com.example.mindkit.feature.modeldownload.domain.DownloadModelUseCase
import com.example.mindkit.feature.modeldownload.domain.InitializeLocalAiUseCase
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.feature.modeldownload.presentation.ModelDownloadViewModel
import com.example.mindkit.ui.viewmodel.HomeViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private fun coreModule(): Module = module {
    single<HttpClient> { createHttpClient() }
    single { ApiService(get()) }

    viewModel { HomeViewModel() }

    single { PromptBuilder() }
    single { LocalAiRepository(get()) }
    single { SendPromptUseCase(get(), get(), get<LocalModelManifest>()) }
    single { DownloadModelUseCase(get()) }
    single { CheckModelAvailabilityUseCase(get()) }
    single { InitializeLocalAiUseCase(get(), get(), get()) }

    viewModel { ChatViewModel(get(), get(), get()) }
    viewModel {
        ModelDownloadViewModel(
            manifest = get(),
            downloadModel = get(),
            checkModelAvailability = get(),
            initializeLocalAi = get(),
            localAiRepository = get(),
        )
    }
}

val appModule = listOf(
    coreModule(),
    platformModule()
)
