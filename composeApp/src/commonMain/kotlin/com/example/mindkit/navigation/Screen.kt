package com.example.mindkit.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Home : Screen

    @Serializable
    data object Permissions : Screen

    @Serializable
    data object Notifications : Screen
}