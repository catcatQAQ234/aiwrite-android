package com.aiwrite.ui.screens.creativehub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiwrite.domain.model.DirectorPhase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreativeHubScreen(
    viewModel: DirectorViewModel = hiltViewModel()
) {
    val progress by viewModel.progress.collectAsState()
    val inspiration by viewModel.inspiration.collectAsState()
    val generatedOptions by viewModel.generatedOptions.collectAsState()
    val planOutput by viewModel.planOutput.collectAsState()
    val worldOutput by viewModel.worldOutput.collectAsState()
    val characterOutput by viewModel.characterOutput.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创作中枢") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                "AI 开书导演",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "从一句灵感走向完整小说。填入你的故事灵感，AI 将自动完成开书策划。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Inspiration input
            OutlinedTextField(
                value = inspiration,
                onValueChange = { viewModel.setInspiration(it) },
                label = { Text("故事灵感") },
                placeholder = { Text("如：一个废柴少年偶然获得了一本能预知未来的笔记本...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                enabled = !progress.isRunning
            )

            // Launch button
            Button(
                onClick = { viewModel.startDirector() },
                enabled = inspiration.isNotBlank() && !progress.isRunning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (progress.phase == DirectorPhase.IDLE) "开始开书" else "运行中...")
            }

            // Progress indicator
            AnimatedVisibility(visible = progress.isRunning) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                progress.currentStep,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = {
                                val total = DirectorPhase.entries.size - 2f // exclude IDLE and READY
                                val current = DirectorPhase.entries.indexOf(progress.phase).toFloat()
                                (current / total).coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Phase pipeline indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DirectorPhase.entries.filter { it != DirectorPhase.IDLE }.forEach { phase ->
                    val isComplete = progress.phase.ordinal > phase.ordinal
                    val isCurrent = progress.phase == phase
                    val color = when {
                        isCurrent -> MaterialTheme.colorScheme.primary
                        isComplete -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = color
                    ) {
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }

            // Output sections
            if (generatedOptions.isNotEmpty()) {
                OutputSection("开书方向", generatedOptions)
            }
            if (planOutput.isNotEmpty()) {
                OutputSection("宏观规划", planOutput)
            }
            if (worldOutput.isNotEmpty()) {
                OutputSection("世界观", worldOutput)
            }
            if (characterOutput.isNotEmpty()) {
                OutputSection("角色阵容", characterOutput)
            }

            // Completion
            if (progress.phase == DirectorPhase.READY_TO_WRITE) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "开书准备完成！",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "世界观、角色阵容、卷战略和章节目录已生成。你可以前往「小说」页面创建项目并开始写作。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutputSection(title: String, content: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "收起" else "展开")
                }
            }

            AnimatedVisibility(visible = expanded) {
                Text(
                    content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
