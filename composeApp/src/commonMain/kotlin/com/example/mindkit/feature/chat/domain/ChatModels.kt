package com.example.mindkit.feature.chat.domain

data class AiGenerationConfig(
    val maxNewTokens: Int = 96,
    val temperature: Float = 0.6f,
    val topP: Float = 0.9f,
    val topK: Int = 32,
    val stopSequences: List<String> = listOf("<end_of_turn>"),
)

data class AiChatRequest(
    val systemInstruction: String,
    val messages: List<AiChatTurn>,
)

data class AiChatTurn(
    val role: ChatRole,
    val content: String,
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

fun AiChatRequest.asPlainPrompt(): String = buildString {
    appendLine(systemInstruction)
    messages.forEach { message ->
        val role = if (message.role == ChatRole.User) "User" else "Assistant"
        appendLine()
        append(role)
        append(": ")
        append(message.content)
    }
    appendLine()
    append("Assistant:")
}
