import Foundation
import ComposeApp

#if canImport(FoundationModels)
import FoundationModels
#endif

final class AppleFoundationModelsService: NSObject, AppleFoundationModelsBridge {
    private var generationTask: Task<Void, Never>?

    func checkAvailability(callback: AppleModelAvailabilityCallback) {
        guard #available(iOS 26.0, *) else {
            callback.onResult(
                available: false,
                statusText: "Apple Foundation Models require iOS 26 or later"
            )
            return
        }

        #if targetEnvironment(simulator)
        callback.onResult(
            available: false,
            statusText: "Apple Intelligence is not available on the Simulator — run on a real device"
        )
        return
        #endif

        #if canImport(FoundationModels)
        switch SystemLanguageModel.default.availability {
        case .available:
            callback.onResult(
                available: true,
                statusText: "Running locally with Apple Foundation Models"
            )
        case .unavailable(.deviceNotEligible):
            callback.onResult(
                available: false,
                statusText: "This device does not support Apple Intelligence"
            )
        case .unavailable(.appleIntelligenceNotEnabled):
            callback.onResult(
                available: false,
                statusText: "Enable Apple Intelligence in Settings to use local AI"
            )
        case .unavailable(.modelNotReady):
            callback.onResult(
                available: false,
                statusText: "Apple Intelligence is preparing its on-device model"
            )
        @unknown default:
            callback.onResult(
                available: false,
                statusText: "Apple Foundation Models are currently unavailable"
            )
        }
        #else
        callback.onResult(
            available: false,
            statusText: "FoundationModels is unavailable in this SDK"
        )
        #endif
    }

    func generate(
        prompt: String,
        maxTokens: Int32,
        temperature: Double,
        topP: Double,
        callback: AppleModelGenerationCallback
    ) {
        cancel()

        guard #available(iOS 26.0, *) else {
            callback.onError(message: "Apple Foundation Models require iOS 26 or later")
            return
        }

        #if canImport(FoundationModels)
        generationTask = Task {
            do {
                let model = SystemLanguageModel.default
                guard model.isAvailable else {
                    callback.onError(message: "Apple Foundation Models are unavailable")
                    return
                }

                let session = LanguageModelSession(model: model)
                let options = GenerationOptions(
                    sampling: temperature > 0
                        ? .random(probabilityThreshold: topP, seed: nil)
                        : .greedy,
                    temperature: temperature,
                    maximumResponseTokens: Int(maxTokens)
                )

                var emittedText = ""
                let stream = session.streamResponse(to: prompt, options: options)
                for try await snapshot in stream {
                    try Task.checkCancellation()
                    let currentText = snapshot.content
                    let delta = currentText.hasPrefix(emittedText)
                        ? String(currentText.dropFirst(emittedText.count))
                        : currentText
                    emittedText = currentText
                    if !delta.isEmpty {
                        callback.onText(text: delta)
                    }
                }
                callback.onComplete()
            } catch is CancellationError {
                // Cancellation is initiated by Kotlin and is not an error.
            } catch {
                callback.onError(message: error.localizedDescription)
            }
        }
        #else
        callback.onError(message: "FoundationModels is unavailable in this SDK")
        #endif
    }

    func cancel() {
        generationTask?.cancel()
        generationTask = nil
    }
}
