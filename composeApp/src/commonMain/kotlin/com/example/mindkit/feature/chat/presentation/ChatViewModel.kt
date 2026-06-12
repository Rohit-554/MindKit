package com.example.mindkit.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindkit.feature.chat.data.LocalAiRepository
import com.example.mindkit.feature.chat.domain.AiTaskMode
import com.example.mindkit.feature.chat.domain.AiToken
import com.example.mindkit.feature.chat.domain.ChatMessage
import com.example.mindkit.feature.chat.domain.ChatRole
import com.example.mindkit.feature.chat.domain.SendPromptUseCase
import com.example.mindkit.feature.modeldownload.domain.InitializeLocalAiUseCase
import com.example.mindkit.feature.modelsettings.domain.ModelState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val sendPrompt: SendPromptUseCase,
    private val initializeLocalAi: InitializeLocalAiUseCase,
    private val localAiRepository: LocalAiRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var generationJob: Job? = null
    private var messageSequence = 0L

    init {
        viewModelScope.launch {
            localAiRepository.modelState.collect { modelState ->
                _state.update { it.copy(modelState = modelState) }
            }
        }
        refreshModel()
    }

    fun onAction(action: ChatAction) {
        when (action) {
            is ChatAction.InputChanged -> _state.update {
                it.copy(input = action.value, errorMessage = null)
            }
            is ChatAction.ModeSelected -> _state.update { it.copy(selectedMode = action.mode) }
            ChatAction.SendClicked -> send()
            ChatAction.StopClicked -> stop()
            ChatAction.ClearChatClicked -> clearChat()
            ChatAction.SettingsClicked -> Unit
        }
    }

    fun refreshModel() {
        viewModelScope.launch {
            initializeLocalAi().onFailure {
                if (localAiRepository.modelState.value !is ModelState.Failed) {
                    localAiRepository.updateModelState(ModelState.NotDownloaded)
                }
            }
        }
    }

    private fun send() {
        if (!_state.value.canSend) return
        val input = _state.value.input.trim()
        val mode = _state.value.selectedMode
        val replyMessage = prepareConversationEntry(input)
        streamReply(mode, input, replyMessage.id)
    }

    private fun prepareConversationEntry(input: String): ChatMessage {
        val userMessage = createChatMessage(ChatRole.User, input)
        val replyMessage = createChatMessage(ChatRole.Assistant, "")
        _state.update {
            it.copy(
                input = "",
                messages = it.messages + userMessage + replyMessage,
                isGenerating = true,
                errorMessage = null,
            )
        }
        return replyMessage
    }

    private fun streamReply(mode: AiTaskMode, input: String, replyId: String) {
        generationJob = viewModelScope.launch {
            sendPrompt(mode, input).collect { token ->
                when (token) {
                    is AiToken.Text -> appendTokenToReply(replyId, token.value)
                    AiToken.Completed -> finishGeneration()
                    is AiToken.Failed -> finishGeneration(token.message)
                }
            }
        }.also { job ->
            job.invokeOnCompletion { error ->
                if (error != null && error !is CancellationException) {
                    _state.update {
                        it.copy(
                            isGenerating = false,
                            errorMessage = error.message ?: "Generation failed",
                        )
                    }
                }
            }
        }
    }

    private fun stop() {
        if (!_state.value.isGenerating) return
        viewModelScope.launch {
            localAiRepository.cancelGeneration()
            generationJob?.cancel()
            finishGeneration()
        }
    }

    private fun clearChat() {
        stop()
        _state.update {
            it.copy(
                input = "",
                messages = emptyList(),
                isGenerating = false,
                errorMessage = null,
            )
        }
    }

    private fun appendTokenToReply(replyId: String, token: String) {
        _state.update { state ->
            state.copy(
                messages = state.messages.map { message ->
                    if (message.id == replyId) message.copy(content = message.content + token)
                    else message
                },
            )
        }
    }

    private fun finishGeneration(errorMessage: String? = null) {
        generationJob = null
        _state.update { it.copy(isGenerating = false, errorMessage = errorMessage) }
    }

    private fun createChatMessage(role: ChatRole, content: String): ChatMessage {
        val sequence = messageSequence++
        return ChatMessage(
            id = sequence.toString(),
            role = role,
            content = content,
            createdAtMillis = sequence,
        )
    }
}
