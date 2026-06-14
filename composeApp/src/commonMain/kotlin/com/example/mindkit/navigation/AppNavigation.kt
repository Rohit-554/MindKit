package com.example.mindkit.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import com.example.mindkit.feature.chat.presentation.ChatScreen
import com.example.mindkit.feature.modeldownload.presentation.ModelDownloadScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Screen.ModelSetup::class)
                    subclass(Screen.Chat::class)
                }
            }
        },
        Screen.ModelSetup,
    )

    Crossfade(targetState = backStack.lastOrNull() ?: Screen.ModelSetup) { screen ->
        when (screen) {
            is Screen.ModelSetup -> ModelDownloadScreen(
                onNavigateToChat = { backStack.add(Screen.Chat) },
            )
            is Screen.Chat -> ChatScreen()
        }
    }
}
