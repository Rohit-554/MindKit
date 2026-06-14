# MindKit

A private, on-device AI assistant built with Kotlin Multiplatform and Compose Multiplatform. Everything runs locally on the device. No cloud, no data sent anywhere.

Android uses a downloaded ONNX model. iOS uses Apple Foundation Models (Apple Intelligence). There is no server.

---

## Demo 
<img width="200" height="450" alt="image" src="https://github.com/user-attachments/assets/966a146f-7890-41c0-a690-d5fc364f1691" />
<img width="200" height="450" alt="image" src="https://github.com/user-attachments/assets/fbaa4a21-dcd3-4ddf-ab81-95d1a917c20c" />
<img width="200" height="450" alt="image" src="https://github.com/user-attachments/assets/b2ec4d42-d194-4289-a9dc-5f0aa134b028" />
<img width="200" height="450" alt="image" src="https://github.com/user-attachments/assets/fe42c589-8525-4a44-96b1-09a819271757" />
<img width="200" height="450" alt="image" src="https://github.com/user-attachments/assets/58df6597-83b7-4659-aa39-ce48fe643c9c" />



## Features

- **Quick Ask** - Get direct answers to anything
- **Explain Code** - Understand what any code snippet does
- **Summarize** - Condense long text into key points
- **Rewrite Reply** - Polish messages with better tone and grammar
- Real-time streaming token output
- Chat history with up to 10 saved conversations
- Stop generation at any point
- Fully offline after model download

---

## Platform Support

| Platform | Status | Notes |
|----------|--------|-------|
| Android | Ready | API 24+, downloads Gemma 3 270M via ONNX Runtime |
| iOS | Ready | iOS 26+, requires Apple Intelligence enabled |
| Desktop | Not supported | Stub only |

---

## Tech Stack

| Layer | Library |
|-------|---------|
| Language | Kotlin 2.3.21 |
| UI | Compose Multiplatform 1.11.0 |
| Navigation | Navigation3 1.1.1 |
| DI | Koin 4.1.1 |
| Networking | Ktor 3.4.3 |
| Android AI | ONNX Runtime GenAI 0.14.0 |
| iOS AI | Apple FoundationModels (Swift) |
| Architecture | MVVM + Clean Architecture |

---

## Architecture

```
UI (Compose)
    |
ViewModel  (StateFlow + Coroutines)
    |
Use Cases  (SendPromptUseCase, DownloadModelUseCase)
    |
Repository (LocalAiRepository, ModelDownloadRepository)
    |
Platform   (LocalAiEngine interface)
    |
  Android: AndroidOnnxLocalAiEngine
  iOS:     IosAppleFoundationAiEngine
```

`LocalAiEngine` is the single abstraction that isolates all platform AI code from the shared domain layer. Swapping models or runtimes only requires a new implementation of this interface.

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later (for Android)
- Xcode 26 beta or later (for iOS)
- JDK 17+

### Android Setup

1. Clone the repository
2. Copy `local.properties.example` to `local.properties`
3. Fill in the model URL and checksum:

```properties
sdk.dir=/path/to/your/Android/sdk

mindkit.model.download.url=https://github.com/Rohit-554/MindKit/releases/download/gemma-3-270m-onnx-v1/gemma-3-270m-onnx.zip
mindkit.model.checksum.sha256=525a3c9f30208638d9bbb40f85e18d345779b2d46760892497f8da63a08d094d
mindkit.model.expected.size.bytes=251500897
```

4. Open the project in Android Studio and run the `androidApp` configuration

The app will download and verify the model on first launch (about 240 MB, one-time).

### iOS Setup

1. Open the project in Android Studio or run from terminal:

```bash
./gradlew :composeApp:iosSimulatorArm64Binaries
```

2. Open `iosApp/iosApp.xcodeproj` in Xcode
3. Set your development team in the project signing settings
4. Run on a physical device with Apple Intelligence enabled

> Apple Foundation Models do not run on the iOS Simulator. A real device running iOS 26 with Apple Intelligence set up is required.

### Build Commands

```bash
# Android debug APK
./gradlew :androidApp:assembleDebug

# iOS framework (simulator)
./gradlew :composeApp:iosSimulatorArm64Binaries

# iOS framework (device)
./gradlew :composeApp:iosArm64Binaries

# Run all tests
./gradlew :composeApp:allTests
```

---

## Using a Different Model (Android)

The app is model-agnostic on Android. Any ONNX Runtime GenAI compatible model bundle works. The ZIP must contain:

- `genai_config.json`
- Model weights (`.onnx` files)
- Tokenizer files

Point `mindkit.model.download.url` to your own hosted ZIP and update the SHA256 checksum.

---

## Generation Configuration

All generation parameters are defined in `AiGenerationConfig` (`composeApp/src/commonMain/kotlin/com/example/mindkit/feature/chat/domain/ChatModels.kt`) and apply to both Android and iOS.

| Parameter | Default | Description |
|-----------|---------|-------------|
| `maxNewTokens` | `96` | Maximum tokens to generate per response |
| `temperature` | `0.6` | Randomness of output. Lower is more focused, higher is more creative |
| `topP` | `0.9` | Nucleus sampling threshold. Keeps the top 90% probability mass |
| `topK` | `32` | Limits sampling to the top K most likely tokens |
| `stopSequences` | `["<end_of_turn>"]` | Tokens that signal the model to stop generating |

To change the defaults, edit `AiGenerationConfig`:

```kotlin
data class AiGenerationConfig(
    val maxNewTokens: Int = 256,       // increase for longer replies
    val temperature: Float = 0.7f,     // 0.0 = deterministic, 1.0 = creative
    val topP: Float = 0.9f,
    val topK: Int = 40,
    val stopSequences: List<String> = listOf("<end_of_turn>"),
)
```

You can also override per-model defaults by passing a custom `AiGenerationConfig` when constructing a `LocalModelManifest` in `DefaultModels`.

---

## Project Structure

```
MindKit/
├── composeApp/
│   └── src/
│       ├── commonMain/         # Shared UI, ViewModels, use cases, domain
│       ├── androidMain/        # ONNX Runtime integration, download worker
│       ├── iosMain/            # Apple Foundation Models Kotlin bridge
│       └── desktopMain/        # Stub (unsupported)
├── androidApp/                 # Android application entry point
└── iosApp/                     # Xcode project + Swift bridge layer
    └── iosApp/
        ├── AppleFoundationModelsService.swift
        └── ContentView.swift
```

---

## License

```
MIT License

Copyright (c) 2025 Rohit Kumar

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
