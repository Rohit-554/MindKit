package com.example.mindkit.feature.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mindkit.feature.chat.domain.AiTaskMode
import com.example.mindkit.feature.chat.domain.ChatMessage
import com.example.mindkit.feature.chat.domain.ChatRole
import com.example.mindkit.feature.modelsettings.domain.ModelState
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material3.rememberDrawerState

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAccuracyNotice by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawer(
                history = state.chatHistory,
                onNewChat = {
                    viewModel.onAction(ChatAction.NewChatClicked)
                    scope.launch { drawerState.close() }
                },
                onHistorySelected = { id ->
                    viewModel.onAction(ChatAction.HistorySelected(id))
                    scope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                ChatTopBar(
                    modelState = state.modelState,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onShowAccuracyNotice = { showAccuracyNotice = true },
                )
            },
            bottomBar = {
                PromptInputBar(
                    input = state.input,
                    canSend = state.canSend,
                    isGenerating = state.isGenerating,
                    onInputChanged = { viewModel.onAction(ChatAction.InputChanged(it)) },
                    onSend = { viewModel.onAction(ChatAction.SendClicked) },
                    onStop = { viewModel.onAction(ChatAction.StopClicked) },
                    onClear = { viewModel.onAction(ChatAction.ClearChatClicked) },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                if (state.shouldShowModeChips) {
                    TaskModeRow(
                        selected = state.selectedMode,
                        onModeSelected = { viewModel.onAction(ChatAction.ModeSelected(it)) },
                    )
                }
                state.errorMessage?.let { error ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
                MessageList(
                    messages = state.messages,
                    isGenerating = state.isGenerating,
                    onShowAccuracyNotice = { showAccuracyNotice = true },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (showAccuracyNotice) {
        AccuracyNoticeDialog(onDismiss = { showAccuracyNotice = false })
    }
}

@Composable
private fun ChatDrawer(
    history: List<ChatHistoryItem>,
    onNewChat: () -> Unit,
    onHistorySelected: (String) -> Unit,
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text("MindKit", style = MaterialTheme.typography.titleLarge)
            }
            NavigationDrawerItem(
                label = { Text("New chat") },
                selected = false,
                onClick = onNewChat,
                icon = {
                    Icon(Icons.Default.AddComment, contentDescription = null)
                },
            )
            if (history.isNotEmpty()) {
                Text(
                    text = "Recent chats",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                )
                history.forEach { chat ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = chat.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        selected = false,
                        onClick = { onHistorySelected(chat.id) },
                        icon = {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    modelState: ModelState,
    onOpenDrawer: () -> Unit,
    onShowAccuracyNotice: () -> Unit,
) {
    val statusLabel = modelState.statusLabel
    val statusColor = modelState.resolveStatusColor()

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open navigation menu",
                )
            }
        },
        title = {
            Column {
                Text(
                    text = "MindKit",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                )
            }
        },
        actions = {
            IconButton(onClick = onShowAccuracyNotice) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About experimental AI accuracy",
                    tint = Color(0xFFC9A84C),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    )
}

@Composable
private fun TaskModeRow(
    selected: AiTaskMode,
    onModeSelected: (AiTaskMode) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(AiTaskMode.entries) { mode ->
            TaskModeChip(
                mode = mode,
                isSelected = mode == selected,
                onSelected = { onModeSelected(mode) },
            )
        }
    }
}

@Composable
private fun TaskModeChip(
    mode: AiTaskMode,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = { Text(mode.title, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = {
            Icon(
                imageVector = mode.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = MaterialTheme.shapes.extraLarge,
    )
}

@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    onShowAccuracyNotice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    if (messages.isEmpty() && !isGenerating) {
        EmptyConversationHint(
            onShowAccuracyNotice = onShowAccuracyNotice,
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(message = message, isGenerating = isGenerating)
        }
    }
}

@Composable
private fun EmptyConversationHint(
    onShowAccuracyNotice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Select a mode above and ask anything.\nEverything runs privately on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(onClick = onShowAccuracyNotice) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFC9A84C),
            )
            Text(
                text = " Experimental AI: review every response",
                color = Color(0xFFC9A84C),
            )
        }
    }
}

@Composable
private fun AccuracyNoticeDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFC9A84C),
            )
        },
        title = { Text("Experimental AI") },
        text = {
            Text(
                "MindKit uses a small on-device experimental model. Its responses can be " +
                    "inaccurate, incomplete, repetitive, or entirely made up.\n\n" +
                    "Review and verify every statement before using or sharing it. Do not rely " +
                    "on responses for medical, legal, financial, safety, or other important " +
                    "decisions.\n\n" +
                    "Hallucinations are expected. Please do not get frustrated; this experiment " +
                    "is still improving, and we are with you.",
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("I understand")
            }
        },
    )
}

@Composable
private fun MessageBubble(message: ChatMessage, isGenerating: Boolean) {
    val isUser = message.role == ChatRole.User
    val showThinkingDots = !isUser && message.content.isEmpty() && isGenerating

    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 24.dp)
    }

    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (showThinkingDots) {
                ThinkingIndicator()
            } else {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                )
            }
        }
    }
}

@Composable
private fun ThinkingIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun PromptInputBar(
    input: String,
    canSend: Boolean,
    isGenerating: Boolean,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onClear: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChanged,
                placeholder = {
                    Text(
                        text = "Ask anything...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
            )

            if (isGenerating) {
                IconButton(onClick = onStop) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop generation",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            } else {
                IconButton(onClick = onSend, enabled = canSend) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send prompt",
                        tint = if (canSend) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (input.isNotEmpty() && !isGenerating) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear chat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private val ModelState.statusLabel: String
    get() = when (this) {
        ModelState.Ready -> "Running locally on device"
        ModelState.Loading -> "Loading model..."
        ModelState.Downloading -> "Downloading..."
        ModelState.Extracting -> "Extracting..."
        ModelState.Verifying -> "Verifying..."
        is ModelState.Failed -> "Error · ${this.reason.take(40)}"
        ModelState.NotDownloaded -> "Model not installed"
        ModelState.Unsupported -> "Not supported on this device"
    }

@Composable
private fun ModelState.resolveStatusColor(): Color = when (this) {
    ModelState.Ready -> MaterialTheme.colorScheme.secondary
    is ModelState.Failed -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private val AiTaskMode.icon: ImageVector
    get() = when (this) {
        AiTaskMode.QuickAsk -> Icons.Default.Bolt
        AiTaskMode.ExplainCode -> Icons.Default.Code
        AiTaskMode.Summarize -> Icons.Default.Description
        AiTaskMode.RewriteReply -> Icons.Default.AutoFixHigh
    }
