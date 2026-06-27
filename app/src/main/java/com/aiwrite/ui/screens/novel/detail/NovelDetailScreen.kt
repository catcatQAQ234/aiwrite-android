package com.aiwrite.ui.screens.novel.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.aiwrite.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelDetailScreen(
    onBack: () -> Unit,
    onChapterClick: (String) -> Unit,
    viewModel: NovelDetailViewModel = hiltViewModel()
) {
    val novel by viewModel.novel.collectAsState()
    val volumes by viewModel.volumes.collectAsState()
    var showCreateVolume by remember { mutableStateOf(false) }
    var expandedVolumes by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(novel?.title ?: "小说详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateVolume = true }) {
                Icon(Icons.Filled.Add, contentDescription = "新建卷")
            }
        }
    ) { padding ->
        if (volumes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "还没有卷，点击 + 创建第一卷",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(volumes, key = { it.id }) { volume ->
                    VolumeCard(
                        volume = volume,
                        isExpanded = expandedVolumes.contains(volume.id),
                        onToggleExpand = {
                            expandedVolumes = if (expandedVolumes.contains(volume.id)) {
                                expandedVolumes - volume.id
                            } else {
                                expandedVolumes + volume.id
                            }
                        },
                        onDeleteVolume = { viewModel.deleteVolume(volume) },
                        onChapterClick = onChapterClick,
                        onCreateChapter = { title ->
                            viewModel.createChapter(volume.id, title)
                        },
                        onDeleteChapter = { viewModel.deleteChapter(it) },
                        chaptersFlow = viewModel.getChapters(volume.id)
                    )
                }
            }
        }
    }

    if (showCreateVolume) {
        CreateVolumeDialog(
            onDismiss = { showCreateVolume = false },
            onCreate = { title ->
                viewModel.createVolume(title)
                showCreateVolume = false
            }
        )
    }
}

@Composable
private fun VolumeCard(
    volume: com.aiwrite.data.local.entity.VolumeEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDeleteVolume: () -> Unit,
    onChapterClick: (String) -> Unit,
    onCreateChapter: (String) -> Unit,
    onDeleteChapter: (ChapterEntity) -> Unit,
    chaptersFlow: Flow<List<ChapterEntity>>
) {
    var showCreateChapter by remember { mutableStateOf(false) }
    val chapters by chaptersFlow.collectAsState(initial = emptyList())

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = volume.title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onDeleteVolume) {
                        Icon(Icons.Filled.Delete, "删除卷", tint = MaterialTheme.colorScheme.error)
                    }
                    Icon(
                        if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "展开/折叠"
                    )
                }
            }

            if (isExpanded) {
                HorizontalDivider()
                // Chapter list
                chapters.forEach { chapter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { onChapterClick(chapter.id) })
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onDeleteChapter(chapter) }) {
                            Icon(
                                Icons.Filled.Delete, "删除章节",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                TextButton(
                    onClick = { showCreateChapter = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加章节")
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    if (showCreateChapter) {
        CreateChapterDialog(
            onDismiss = { showCreateChapter = false },
            onCreate = { title ->
                onCreateChapter(title)
                showCreateChapter = false
            }
        )
    }
}

@Composable
private fun CreateVolumeDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建新卷") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("卷名（如：第一卷·启程）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(title.trim()) },
                enabled = title.isNotBlank()
            ) { Text("创建") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun CreateChapterDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建新章节") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("章节标题") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(title.trim()) },
                enabled = title.isNotBlank()
            ) { Text("创建") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
