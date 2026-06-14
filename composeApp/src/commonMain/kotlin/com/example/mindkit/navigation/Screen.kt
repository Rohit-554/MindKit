package com.example.mindkit.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object ModelSetup : Screen

    @Serializable
    data object Chat : Screen
}
