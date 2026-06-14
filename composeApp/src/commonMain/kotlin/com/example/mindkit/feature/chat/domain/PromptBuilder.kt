package com.example.mindkit.feature.chat.domain

class PromptBuilder {
    fun buildRequest(
        mode: AiTaskMode,
        userInput: String,
        conversationHistory: List<ChatMessage> = emptyList(),
    ): AiChatRequest {
        val input = userInput.trim()
        require(input.isNotEmpty()) { "Prompt input cannot be blank" }

        val instruction = when (mode) {
            AiTaskMode.QuickAsk ->
                "Answer the user directly in at most three short sentences."
            AiTaskMode.ExplainCode ->
                "Explain the code simply: what it does, how it works, and one likely issue."
            AiTaskMode.Summarize ->
                "Summarize the user's text in exactly three short bullet points."
            AiTaskMode.RewriteReply ->
                "Write only the final first-person message the user can send. " +
                    "Use the conversation for context. Never repeat the request."
        }

        val recent = conversationHistory
            .filter { it.content.isNotBlank() }
            .takeLast(MAX_HISTORY_MESSAGES)
        val history = if (
            recent.size == 2 &&
            recent[0].role == ChatRole.User &&
            recent[1].role == ChatRole.Assistant
        ) {
            recent.map { AiChatTurn(it.role, it.content.trim()) }
        } else {
            emptyList()
        }

        return AiChatRequest(
            systemInstruction = instruction,
            messages = history + AiChatTurn(ChatRole.User, input),
        )
    }

    private companion object {
        const val MAX_HISTORY_MESSAGES = 2
    }
}
