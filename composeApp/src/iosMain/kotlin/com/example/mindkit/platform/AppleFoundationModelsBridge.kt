package com.example.mindkit.platform

interface AppleModelAvailabilityCallback {
    fun onResult(available: Boolean, statusText: String)
}

interface AppleModelGenerationCallback {
    fun onText(text: String)
    fun onComplete()
    fun onError(message: String)
}

interface AppleFoundationModelsBridge {
    fun checkAvailability(callback: AppleModelAvailabilityCallback)

    fun generate(
        prompt: String,
        maxTokens: Int,
        temperature: Double,
        topP: Double,
        callback: AppleModelGenerationCallback,
    )

    fun cancel()
}

private var registeredBridge: AppleFoundationModelsBridge? = null

fun registerAppleFoundationModelsBridge(bridge: AppleFoundationModelsBridge) {
    registeredBridge = bridge
}

internal fun requireAppleFoundationModelsBridge(): AppleFoundationModelsBridge =
    checkNotNull(registeredBridge) {
        "Apple Foundation Models bridge was not registered by the iOS host"
    }
