package com.example.mindkit.feature.modeldownload.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindkit.feature.chat.data.LocalAiRepository
import com.example.mindkit.feature.modeldownload.domain.CheckModelAvailabilityUseCase
import com.example.mindkit.feature.modeldownload.domain.DownloadModelUseCase
import com.example.mindkit.feature.modeldownload.domain.InitializeLocalAiUseCase
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import com.example.mindkit.feature.modeldownload.domain.ModelDownloadState
import com.example.mindkit.feature.modelsettings.domain.ModelState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ModelDownloadViewModel(
    manifest: LocalModelManifest,
    private val downloadModel: DownloadModelUseCase,
    private val checkModelAvailability: CheckModelAvailabilityUseCase,
    private val initializeLocalAi: InitializeLocalAiUseCase,
    private val localAiRepository: LocalAiRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ModelDownloadUiState(manifest))
    val state: StateFlow<ModelDownloadUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (checkModelAvailability()) {
                initializeLocalAi().onSuccess {
                    _state.update {
                        it.copy(
                            isCheckingModel = false,
                            downloadState = ModelDownloadState.Ready(null),
                        )
                    }
                }.onFailure(::showFailure)
            } else {
                _state.update { it.copy(isCheckingModel = false) }
            }
        }
    }

    fun onAction(action: ModelDownloadAction) {
        when (action) {
            ModelDownloadAction.DownloadClicked,
            ModelDownloadAction.RetryClicked -> startDownload()
        }
    }

    private fun startDownload() {
        viewModelScope.launch {
            _state.update { it.copy(isCheckingModel = false) }
            downloadModel().collect { downloadState ->
                _state.update {
                    it.copy(
                        downloadState = downloadState,
                        errorMessage = (downloadState as? ModelDownloadState.Failed)?.reason,
                    )
                }
                localAiRepository.updateModelState(downloadState.toModelState())

                if (downloadState is ModelDownloadState.Ready) {
                    initializeLocalAi().onFailure(::showFailure)
                }
            }
        }
    }

    private fun showFailure(error: Throwable) {
        val message = error.message ?: "Unable to initialize local AI"
        localAiRepository.updateModelState(ModelState.Failed(message))
        _state.update {
            it.copy(
                isCheckingModel = false,
                downloadState = ModelDownloadState.Failed(message),
                errorMessage = message,
            )
        }
    }

    private fun ModelDownloadState.toModelState(): ModelState = when (this) {
        ModelDownloadState.NotDownloaded -> ModelState.NotDownloaded
        is ModelDownloadState.DownloadingZip -> ModelState.Downloading
        ModelDownloadState.VerifyingZip,
        ModelDownloadState.VerifyingExtractedFiles -> ModelState.Verifying
        is ModelDownloadState.ExtractingZip -> ModelState.Extracting
        is ModelDownloadState.Ready -> ModelState.Loading
        is ModelDownloadState.Failed -> ModelState.Failed(reason)
    }
}
