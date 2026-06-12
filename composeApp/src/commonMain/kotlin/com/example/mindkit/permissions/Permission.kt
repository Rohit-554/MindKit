package com.example.mindkit.permissions

enum class Permission {
    NOTIFICATIONS,
    RECORD_AUDIO,
    STORAGE,
}

enum class PermissionStatus {
    GRANTED,
    DENIED,
    DENIED_ALWAYS,
    NOT_DETERMINED,
}