package com.example.mindkit.feature.modeldownload.presentation

import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.feature.modeldownload.domain.ModelDownloadState

data class ModelDownloadUiState(
    val manifest: LocalModelManifest,
    val isCheckingModel: Boolean = true,
    val downloadState: ModelDownloadState = ModelDownloadState.NotDownloaded,
    val errorMessage: String? = null,
)

sealed interface ModelDownloadAction {
    data object DownloadClicked : ModelDownloadAction
    data object RetryClicked : ModelDownloadAction
}
