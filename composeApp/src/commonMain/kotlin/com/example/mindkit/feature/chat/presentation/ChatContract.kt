package com.example.mindkit.feature.chat.presentation

import com.example.mindkit.feature.chat.domain.AiTaskMode
import com.example.mindkit.feature.chat.domain.ChatMessage
import com.example.mindkit.feature.modelsettings.domain.ModelState

data class ChatHistoryItem(
    val id: String,
    val title: String,
    val mode: AiTaskMode,
    val messages: List<ChatMessage>,
)

data class ChatUiState(
    val modelState: ModelState = ModelState.NotDownloaded,
    val selectedMode: AiTaskMode = AiTaskMode.QuickAsk,
    val input: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val chatHistory: List<ChatHistoryItem> = emptyList(),
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
) {
    val shouldShowModeChips: Boolean
        get() = messages.isEmpty() && !isGenerating

    val canSend: Boolean
        get() = modelState == ModelState.Ready && input.isNotBlank() && !isGenerating
}

sealed interface ChatAction {
    data class InputChanged(val value: String) : ChatAction
    data class ModeSelected(val mode: AiTaskMode) : ChatAction
    data object SendClicked : ChatAction
    data object StopClicked : ChatAction
    data object NewChatClicked : ChatAction
    data class HistorySelected(val id: String) : ChatAction
    data object ClearChatClicked : ChatAction
    data object SettingsClicked : ChatAction
}
