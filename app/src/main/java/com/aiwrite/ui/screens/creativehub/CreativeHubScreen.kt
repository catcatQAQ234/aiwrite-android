package com.aiwrite.ui.screens.creativehub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
    val error by viewModel.errorMessage.collectAsState()
    val options by viewModel.generatedOptions.collectAsState()
    val plan by viewModel.planOutput.collectAsState()
    val world by viewModel.worldOutput.collectAsState()
    val characters by viewModel.characterOutput.collectAsState()
    val strategy by viewModel.strategyOutput.collectAsState()
    val chapters by viewModel.chapterBreakdown.collectAsState()
    val selectedOption by viewModel.selectedOptionIndex.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创作中枢") },
                actions = {
                    if (progress.phase != DirectorPhase.IDLE) {
                        IconButton(onClick = { viewModel.clear() }) {
                            Icon(Icons.Filled.Delete, "重新开始")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Text("AI 开书导演", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "从一句灵感走向完整小说。填入灵感，AI 分步引导你的创作。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Phase pipeline indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "💡" to DirectorPhase.OPTIONS_READY,
                    "📋" to DirectorPhase.PLAN_READY,
                    "🌍" to DirectorPhase.WORLD_READY,
                    "👤" to DirectorPhase.CHARACTERS_READY,
                    "📚" to DirectorPhase.STRATEGY_READY,
                    "✍️" to DirectorPhase.READY_TO_WRITE
                ).forEach { (icon, phase) ->
                    val done = progress.phase.ordinal >= phase.ordinal
                    Surface(
                        modifier = Modifier.weight(1f).height(28.dp),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = if (done) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(icon, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Inspiration input
            OutlinedTextField(
                value = inspiration,
                onValueChange = { viewModel.setInspiration(it) },
                label = { Text("故事灵感") },
                placeholder = { Text("如：废柴少年偶然获得了一本能预知未来的笔记本...") },
                minLines = 3, maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                enabled = !progress.isRunning
            )

            // Action button based on phase
            when (progress.phase) {
                DirectorPhase.IDLE -> {
                    Button(
                        onClick = { viewModel.generateOptions() },
                        enabled = inspiration.isNotBlank() && !progress.isRunning,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.AutoAwesome, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始开书")
                    }
                }
                else -> {
                    // Progress indicator
                    AnimatedVisibility(visible = progress.isRunning) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(progress.currentStep, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (!progress.isRunning) {
                        Text(
                            "👆 ${progress.currentStep}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Interactive content per phase
            AnimatedVisibility(visible = options.isNotEmpty() && progress.phase == DirectorPhase.OPTIONS_READY, enter = fadeIn(), exit = fadeOut()) {
                OptionsPhase(
                    options = options,
                    selectedOption = selectedOption,
                    onSelect = { viewModel.selectOptionAndPlan(it) },
                    onRegenerate = { viewModel.generateOptions() }
                )
            }

            AnimatedVisibility(visible = plan.isNotEmpty() && progress.phase == DirectorPhase.PLAN_READY, enter = fadeIn(), exit = fadeOut()) {
                OutputPhase(title = "宏观规划", content = plan, onContinue = { viewModel.continueToWorld() }, onRegenerate = { viewModel.regenerate() })
            }

            AnimatedVisibility(visible = world.isNotEmpty() && progress.phase == DirectorPhase.WORLD_READY, enter = fadeIn(), exit = fadeOut()) {
                OutputPhase(title = "世界观", content = world, onContinue = { viewModel.continueToCharacters() }, onRegenerate = { viewModel.regenerate() })
            }

            AnimatedVisibility(visible = characters.isNotEmpty() && progress.phase == DirectorPhase.CHARACTERS_READY, enter = fadeIn(), exit = fadeOut()) {
                OutputPhase(title = "角色阵容", content = characters, onContinue = { viewModel.continueToStrategy() }, onRegenerate = { viewModel.regenerate() })
            }

            AnimatedVisibility(visible = strategy.isNotEmpty() && progress.phase == DirectorPhase.STRATEGY_READY, enter = fadeIn(), exit = fadeOut()) {
                OutputPhase(title = "卷战略", content = strategy, onContinue = { viewModel.continueToChapters() }, onRegenerate = { viewModel.regenerate() })
            }

            AnimatedVisibility(visible = chapters.isNotEmpty() && progress.phase == DirectorPhase.READY_TO_WRITE, enter = fadeIn(), exit = fadeOut()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("开书准备完成！", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        var expanded by remember { mutableStateOf(false) }
                        TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "收起章节目录" else "查看章节目录") }
                        AnimatedVisibility(visible = expanded) {
                            Text(chapters, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.clear() }, modifier = Modifier.fillMaxWidth()) {
                            Text("开始新的开书")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OptionsPhase(
    options: String,
    selectedOption: Int,
    onSelect: (Int) -> Unit,
    onRegenerate: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("选择开书方向", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            val parts = options.split(Regex("(?=方案[一二三])"))
            parts.filter { it.isNotBlank() }.forEachIndexed { index, part ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index == selectedOption) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = { onSelect(index) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            part.lines().firstOrNull() ?: "方案${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(part, style = MaterialTheme.typography.bodySmall, maxLines = 5)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRegenerate) { Text("重新生成") }
                Button(
                    onClick = { if (selectedOption >= 0) onSelect(selectedOption) },
                    enabled = selectedOption >= 0,
                    modifier = Modifier.weight(1f)
                ) { Text("确认选择，开始规划") }
            }
        }
    }
}

@Composable
private fun OutputPhase(
    title: String,
    content: String,
    onContinue: () -> Unit,
    onRegenerate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$title 输出", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                content.take(200) + if (content.length > 200) "..." else "",
                style = MaterialTheme.typography.bodySmall
            )

            if (content.length > 200) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "收起" else "展开全文")
                }
            }

            AnimatedVisibility(visible = expanded) {
                Text(content, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRegenerate) { Text("重新生成") }
                Button(onClick = onContinue, modifier = Modifier.weight(1f)) { Text("确认，继续下一步") }
            }
        }
    }
}
