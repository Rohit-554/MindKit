package com.example.mindkit.feature.chat.domain

import com.example.mindkit.feature.chat.data.LocalAiRepository
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import kotlinx.coroutines.flow.Flow

class SendPromptUseCase(
    private val promptBuilder: PromptBuilder,
    private val localAiRepository: LocalAiRepository,
    private val activeManifest: LocalModelManifest,
) {
    operator fun invoke(
        mode: AiTaskMode,
        userInput: String,
        conversationHistory: List<ChatMessage>,
    ): Flow<AiToken> {
        val request = promptBuilder.buildRequest(mode, userInput, conversationHistory)
        return localAiRepository.generate(request, activeManifest.generationDefaults)
    }
}
