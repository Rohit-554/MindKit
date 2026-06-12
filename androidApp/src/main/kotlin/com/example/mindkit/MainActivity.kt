package com.example.mindkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mindkit.android.BuildConfig
import com.example.mindkit.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // See local.properties.example for setup instructions.

        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(appModule)
        }

        setContent {
            App()
        }
    }
}
