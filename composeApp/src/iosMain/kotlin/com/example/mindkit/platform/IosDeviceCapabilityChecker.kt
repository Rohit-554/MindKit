package com.example.mindkit.platform

import com.example.mindkit.core.platform.DeviceCapabilityChecker
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class IosDeviceCapabilityChecker : DeviceCapabilityChecker {
    override suspend fun isLocalAiSupported(): Boolean =
        suspendCancellableCoroutine { continuation ->
            requireAppleFoundationModelsBridge().checkAvailability(
                object : AppleModelAvailabilityCallback {
                    override fun onResult(available: Boolean, statusText: String) {
                        if (continuation.isActive) continuation.resume(available)
                    }
                }
            )
        }
}
