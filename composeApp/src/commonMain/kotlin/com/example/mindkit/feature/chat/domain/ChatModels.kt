package com.example.mindkit.feature.chat.domain

data class AiGenerationConfig(
    val maxNewTokens: Int = 256,
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val stopSequences: List<String> = emptyList(),
)

sealed interface AiToken {
    data class Text(val value: String) : AiToken
    data object Completed : AiToken
    data class Failed(val message: String) : AiToken
}

enum class AiTaskMode(
    val title: String,
    val subtitle: String,
) {
    QuickAsk("Quick Ask", "Ask anything short"),
    ExplainCode("Explain Code", "Understand code"),
    Summarize("Summarize", "Short summary"),
    RewriteReply("Rewrite Reply", "Improve message"),
}

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val createdAtMillis: Long,
)

enum class ChatRole {
    User,
    Assistant,
}
