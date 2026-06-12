package com.example.mindkit.feature.chat.domain

class PromptBuilder {
    fun buildPrompt(mode: AiTaskMode, userInput: String): String {
        val input = userInput.trim()
        require(input.isNotEmpty()) { "Prompt input cannot be blank" }

        return when (mode) {
            AiTaskMode.QuickAsk -> """
                Give a short, practical answer.
                Keep the response simple and useful.

                Question:
                $input
            """.trimIndent()

            AiTaskMode.ExplainCode -> """
                Explain this code in simple language.
                Focus on what it does, why it works, and any possible issue.
                Keep the explanation beginner-friendly.

                Code:
                $input
            """.trimIndent()

            AiTaskMode.Summarize -> """
                Summarize the following text in 3 simple bullet points.
                Keep it short and clear.

                Text:
                $input
            """.trimIndent()

            AiTaskMode.RewriteReply -> """
                Rewrite this message to sound clear, polite, and professional.
                Keep it short.

                Message:
                $input
            """.trimIndent()
        }
    }
}
