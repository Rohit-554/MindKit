package com.example.mindkit

import androidx.compose.runtime.Composable
import com.example.mindkit.navigation.AppNavigation
import com.example.mindkit.ui.theme.MindKitTheme

@Composable
fun App() {
    MindKitTheme {
        AppNavigation()
    }
}
