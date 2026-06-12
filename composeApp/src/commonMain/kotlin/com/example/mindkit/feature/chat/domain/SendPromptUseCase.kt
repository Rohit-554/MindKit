package com.example.mindkit.feature.chat.domain

import com.example.mindkit.feature.chat.data.LocalAiRepository
import com.example.mindkit.feature.modeldownload.domain.LocalModelManifest
import kotlinx.coroutines.flow.Flow

class SendPromptUseCase(
    private val promptBuilder: PromptBuilder,
    private val localAiRepository: LocalAiRepository,
    private val activeManifest: LocalModelManifest,
) {
    operator fun invoke(mode: AiTaskMode, userInput: String): Flow<AiToken> {
        val finalPrompt = promptBuilder.buildPrompt(mode, userInput)
        return localAiRepository.generate(finalPrompt, activeManifest.generationDefaults)
    }
}
