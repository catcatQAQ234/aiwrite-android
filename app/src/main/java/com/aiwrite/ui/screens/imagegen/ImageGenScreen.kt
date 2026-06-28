package com.aiwrite.ui.screens.imagegen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aiwrite.domain.model.SCHEDULERS
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGenScreen(
    viewModel: ImageGenViewModel = hiltViewModel()
) {
    val params by viewModel.params.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val engineReady by viewModel.engineReady.collectAsState()
    val history by viewModel.history.collectAsState()

    var showAdvanced by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("文生图") },
                actions = {
                    if (!engineReady) {
                        TextButton(onClick = { }) {
                            Text("下载模型", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(onClick = { showHistory = !showHistory }) {
                        Icon(Icons.Filled.History, "历史")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (showHistory) {
            HistoryPanel(
                history = history,
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onDelete = { viewModel.deleteHistoryItem(it) },
                onSaveToGallery = { viewModel.saveToGallery(it) },
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Prompt input
                OutlinedTextField(
                    value = params.prompt,
                    onValueChange = { viewModel.updatePrompt(it) },
                    label = { Text("提示词") },
                    placeholder = { Text("描述你想生成的画面...") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = params.negativePrompt,
                    onValueChange = { viewModel.updateNegativePrompt(it) },
                    label = { Text("负向提示词") },
                    placeholder = { Text("不想看到的元素...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Size selector
                Text("图片尺寸", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sizes = listOf(512 to 512, 512 to 768, 768 to 512, 768 to 768)
                    sizes.forEach { (w, h) ->
                        FilterChip(
                            selected = params.width == w && params.height == h,
                            onClick = { viewModel.updateWidth(w); viewModel.updateHeight(h) },
                            label = { Text("${w}x${h}", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Steps slider
                Text("采样步数: ${params.steps}", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = params.steps.toFloat(),
                    onValueChange = { viewModel.updateSteps(it.toInt()) },
                    valueRange = 5f..50f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                // CFG scale
                Text("CFG 引导强度: ${params.cfgScale}", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = params.cfgScale,
                    onValueChange = { viewModel.updateCfgScale(it) },
                    valueRange = 1f..20f,
                    modifier = Modifier.fillMaxWidth()
                )

                // Advanced toggle
                TextButton(onClick = { showAdvanced = !showAdvanced }) {
                    Text(if (showAdvanced) "收起高级选项" else "展开高级选项")
                    Icon(
                        if (showAdvanced) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (showAdvanced) {
                    // Seed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = if (params.seed == -1L) "" else params.seed.toString(),
                            onValueChange = { viewModel.updateSeed(it.toLongOrNull() ?: -1L) },
                            label = { Text("种子（留空随机）") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.randomSeed() }) {
                            Icon(Icons.Filled.Shuffle, "随机种子")
                        }
                    }

                    // Scheduler
                    Text("采样器", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SCHEDULERS.take(4).forEach { s ->
                            FilterChip(
                                selected = params.scheduler == s,
                                onClick = { viewModel.updateScheduler(s) },
                                label = { Text(s, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    // CPU toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = params.useCpu, onCheckedChange = { viewModel.toggleCpu() })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("强制使用 CPU", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Generate button
                Button(
                    onClick = { viewModel.generate() },
                    enabled = params.prompt.isNotBlank() && engineReady && !isGenerating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("生成中...")
                    } else {
                        Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("生成")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HistoryPanel(
    history: List<com.aiwrite.data.local.entity.GenerationHistoryEntity>,
    onToggleFavorite: (Long) -> Unit,
    onDelete: (com.aiwrite.data.local.entity.GenerationHistoryEntity) -> Unit,
    onSaveToGallery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("还没有生成历史", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history, key = { it.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        // Thumbnail
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(item.imagePath))
                                .crossfade(true)
                                .build(),
                            contentDescription = item.prompt,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(MaterialTheme.shapes.small),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.prompt.take(60),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2
                            )
                            Text(
                                "${item.width}x${item.height} | ${item.steps}步 | ${item.scheduler}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                item.generationTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column {
                            IconButton(onClick = { onToggleFavorite(item.id) }) {
                                Icon(
                                    if (item.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    "收藏",
                                    tint = if (item.favorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onSaveToGallery(item.imagePath) }) {
                                Icon(Icons.Filled.SaveAlt, "保存到相册")
                            }
                            IconButton(onClick = { onDelete(item) }) {
                                Icon(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
