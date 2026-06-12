package com.example.mindkit.notifications

enum class AppNotificationChannel(
    val channelId: String,
    val channelName: String,
) {
    REMINDERS(channelId = "catylst_reminders", channelName = "Reminders"),
    ALERTS(channelId = "catylst_alerts", channelName = "Alerts"),
    PROMOTIONS(channelId = "catylst_promotions", channelName = "Promotions"),
}
