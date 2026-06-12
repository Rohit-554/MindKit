package com.example.mindkit.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import com.example.mindkit.ui.screens.HomeScreen
import com.example.mindkit.ui.screens.NotificationDemoScreen
import com.example.mindkit.ui.screens.PermissionDemoScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Screen.Home::class)
                    subclass(Screen.Permissions::class)
                    subclass(Screen.Notifications::class)
                }
            }
        },
        Screen.Home
    )

    Crossfade(targetState = backStack.lastOrNull() ?: Screen.Home) { screen ->
        when (screen) {
            is Screen.Home -> HomeScreen(
                onNavigateToPermissions = { backStack.add(Screen.Permissions) },
                onNavigateToNotifications = { backStack.add(Screen.Notifications) },
            )
            is Screen.Permissions -> PermissionDemoScreen(
                onBack = { backStack.removeLastOrNull() },
            )
            is Screen.Notifications -> NotificationDemoScreen(
                onBack = { backStack.removeLastOrNull() },
            )
        }
    }
}