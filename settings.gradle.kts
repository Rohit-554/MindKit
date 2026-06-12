rootProject.name = "MindKit"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        ivy {
            name = "OnnxRuntimeGenAiReleases"
            url = uri("https://github.com/microsoft/onnxruntime-genai/releases/download")
            patternLayout {
                artifact("v[revision]/[artifact]-[revision].[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("ai.onnxruntime.genai", "onnxruntime-genai-android")
            }
        }
    }
}

include(":composeApp")
include(":androidApp")
