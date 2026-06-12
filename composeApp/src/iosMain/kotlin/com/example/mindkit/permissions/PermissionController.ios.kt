package com.example.mindkit.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatus
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

actual class PermissionController {

    actual suspend fun checkPermission(permission: Permission): PermissionStatus =
        when (permission) {
            Permission.RECORD_AUDIO -> avStatus(AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio))
            Permission.NOTIFICATIONS -> notificationStatus()
            Permission.STORAGE -> PermissionStatus.GRANTED
        }

    actual suspend fun requestPermission(permission: Permission): PermissionStatus =
        when (permission) {
            Permission.RECORD_AUDIO -> requestAvAccess(AVMediaTypeAudio)
            Permission.NOTIFICATIONS -> requestNotifications()
            Permission.STORAGE -> PermissionStatus.GRANTED
        }

    actual fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url)
    }

    private suspend fun notificationStatus(): PermissionStatus =
        suspendCancellableCoroutine { cont ->
            UNUserNotificationCenter.currentNotificationCenter()
                .getNotificationSettingsWithCompletionHandler { settings ->
                    val status = when (settings?.authorizationStatus) {
                        UNAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
                        UNAuthorizationStatusDenied -> PermissionStatus.DENIED
                        else -> PermissionStatus.NOT_DETERMINED
                    }
                    cont.resume(status)
                }
        }

    private suspend fun requestAvAccess(mediaType: String?): PermissionStatus =
        suspendCancellableCoroutine { cont ->
            AVCaptureDevice.requestAccessForMediaType(mediaType) { granted ->
                cont.resume(if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED)
            }
        }

    private suspend fun requestNotifications(): PermissionStatus =
        suspendCancellableCoroutine { cont ->
            val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            UNUserNotificationCenter.currentNotificationCenter()
                .requestAuthorizationWithOptions(options) { granted, _ ->
                    cont.resume(if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED)
                }
        }

    private fun avStatus(status: AVAuthorizationStatus): PermissionStatus = when (status) {
        AVAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
        AVAuthorizationStatusDenied -> PermissionStatus.DENIED
        AVAuthorizationStatusRestricted -> PermissionStatus.DENIED_ALWAYS
        else -> PermissionStatus.NOT_DETERMINED
    }
}

@Composable
actual fun rememberPermissionController(): PermissionController = remember { PermissionController() }