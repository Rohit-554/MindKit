package com.example.mindkit.notifications

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

expect class NotificationScheduler {
    fun schedule(
        id: String,
        title: String,
        body: String,
        delaySeconds: Int,
        channel: AppNotificationChannel,
    )

    fun cancel(id: String)

    fun cancelAll()
}

fun NotificationScheduler.schedule(
    id: String,
    title: String,
    body: String,
    delaySeconds: Int = 0,
    channel: AppNotificationChannel = AppNotificationChannel.REMINDERS,
) = schedule(id, title, body, delaySeconds, channel)

@Composable
fun rememberNotificationScheduler(): NotificationScheduler = koinInject()
