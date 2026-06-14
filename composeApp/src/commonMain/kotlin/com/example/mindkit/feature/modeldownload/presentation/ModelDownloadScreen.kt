package com.example.mindkit.feature.modeldownload.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mindkit.feature.modeldownload.domain.ModelDownloadState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ModelDownloadScreen(
    onNavigateToChat: () -> Unit,
    viewModel: ModelDownloadViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.downloadState) {
        if (state.downloadState is ModelDownloadState.Ready) onNavigateToChat()
    }

    if (state.isCheckingModel || state.downloadState is ModelDownloadState.Ready) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            ModelLoadingScreen()
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            BrandingSection()
            Spacer(modifier = Modifier.height(48.dp))
            FeatureGrid()
            Spacer(modifier = Modifier.height(48.dp))
            DownloadSection(
                downloadState = state.downloadState,
                errorMessage = state.errorMessage,
                onDownload = { viewModel.onAction(ModelDownloadAction.DownloadClicked) },
                onRetry = { viewModel.onAction(ModelDownloadAction.RetryClicked) },
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (state.downloadState.isInProgress) {
        DownloadProgressDialog(state.downloadState)
    }
}

@Composable
private fun ModelLoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "MindKit",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(32.dp))
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading your on-device model...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BrandingSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "MindKit",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Private AI. On your device.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "No internet required. No data leaves your phone.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FeatureGrid() {
    val features = listOf(
        Feature(Icons.Default.Bolt, "Quick Ask", "Instant answers to anything"),
        Feature(Icons.Default.Code, "Explain Code", "Break down any code snippet"),
        Feature(Icons.Default.Description, "Summarize", "Shorten long text instantly"),
        Feature(Icons.Default.AutoFixHigh, "Rewrite", "Fix tone and grammar"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        features.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { feature ->
                    FeatureCard(
                        feature = feature,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(feature: Feature, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = feature.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = feature.description,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DownloadSection(
    downloadState: ModelDownloadState,
    errorMessage: String?,
    onDownload: () -> Unit,
    onRetry: () -> Unit,
) {
    val hasFailed = downloadState is ModelDownloadState.Failed

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!hasFailed) {
            Button(
                onClick = onDownload,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Icon(
                    imageVector = Icons.Default.DownloadForOffline,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "  Download AI Model",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text(
                text = "~240 MB · One-time download · Free",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = errorMessage ?: "Download failed. Please try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Text("Retry Download")
            }
        }
    }
}

@Composable
private fun DownloadProgressDialog(downloadState: ModelDownloadState) {
    val statusLabel = downloadState.statusLabel
    val progress = downloadState.overallProgress
    val bytesLabel = downloadState.bytesLabel

    AlertDialog(
        onDismissRequest = {},
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large,
        title = {
            Text(
                text = "Setting up your AI...",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = bytesLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (progress != null) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {},
    )
}

private data class Feature(val icon: ImageVector, val title: String, val description: String)

private val ModelDownloadState.isInProgress: Boolean
    get() = this is ModelDownloadState.DownloadingZip ||
        this is ModelDownloadState.ExtractingZip ||
        this == ModelDownloadState.VerifyingZip ||
        this == ModelDownloadState.VerifyingExtractedFiles

private val ModelDownloadState.statusLabel: String
    get() = when (this) {
        is ModelDownloadState.DownloadingZip -> "Downloading AI model..."
        is ModelDownloadState.ExtractingZip -> "Extracting files..."
        ModelDownloadState.VerifyingZip -> "Verifying download..."
        ModelDownloadState.VerifyingExtractedFiles -> "Verifying model files..."
        else -> ""
    }

private val ModelDownloadState.overallProgress: Float?
    get() = when (this) {
        is ModelDownloadState.DownloadingZip -> progress?.let { it * 0.75f }
        is ModelDownloadState.ExtractingZip -> progress?.let { 0.75f + it * 0.2f }
        ModelDownloadState.VerifyingZip -> 0.95f
        ModelDownloadState.VerifyingExtractedFiles -> 0.98f
        else -> null
    }

private val ModelDownloadState.bytesLabel: String
    get() = when {
        this is ModelDownloadState.DownloadingZip && downloadedBytes > 0 -> {
            val downloaded = formatBytes(downloadedBytes)
            val total = totalBytes?.let { " / ${formatBytes(it)}" } ?: ""
            "$downloaded$total"
        }
        else -> ""
    }

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000_000L -> "${oneDecimal(bytes / 1_000_000_000.0)} GB"
    bytes >= 1_000_000L -> "${oneDecimal(bytes / 1_000_000.0)} MB"
    bytes >= 1_000L -> "${oneDecimal(bytes / 1_000.0)} KB"
    else -> "$bytes B"
}

private fun oneDecimal(value: Double): String {
    val rounded = kotlin.math.round(value * 10) / 10.0
    val intPart = rounded.toLong()
    val decPart = kotlin.math.abs(kotlin.math.round(rounded * 10) - intPart * 10)
    return "$intPart.$decPart"
}
