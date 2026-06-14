package com.example.mindkit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MindKitColorScheme = darkColorScheme(
    primary = Violet80,
    onPrimary = VioletDark,
    primaryContainer = VioletContainer,
    onPrimaryContainer = VioletOnContainer,
    inversePrimary = VioletInverse,
    secondary = Cyan80,
    onSecondary = CyanDark,
    secondaryContainer = CyanContainer,
    onSecondaryContainer = CyanOnContainer,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = OutlineDefault,
    outlineVariant = OutlineVariant,
    error = ErrorDefault,
    onError = ErrorDark,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorOnContainer,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
)

@Composable
fun MindKitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MindKitColorScheme,
        typography = MindKitTypography,
        shapes = MindKitShapes,
        content = content,
    )
}
