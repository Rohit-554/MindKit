package com.example.mindkit

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
