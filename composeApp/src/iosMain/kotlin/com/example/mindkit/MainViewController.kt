package com.example.mindkit

import androidx.compose.ui.window.ComposeUIViewController
import com.example.mindkit.di.appModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() }
) { App() }
