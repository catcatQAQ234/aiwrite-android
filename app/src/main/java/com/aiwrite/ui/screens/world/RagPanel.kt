package com.aiwrite.ui.screens.world

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.aiwrite.data.local.entity.KnowledgeItemEntity

@Composable
fun RagPanel(
    items: List<KnowledgeItemEntity>,
    searchQuery: String,
    searchResults: List<KnowledgeItemEntity>,
    isSearching: Boolean,
    lastSemanticResults: List<KnowledgeItemEntity>,
    onCreateItem: (String, String, String) -> Unit,
    onImportText: (String, String) -> Unit,
    onDelete: (KnowledgeItemEntity) -> Unit,
    onSearch: (String) -> Unit,
    onSemanticSearch: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var localSearchQuery by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(0) } // 0=keyword, 1=semantic

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = localSearchQuery,
                    onValueChange = {
                        localSearchQuery = it
                        if (searchMode == 0) onSearch(it)
                    },
                    placeholder = { Text("搜索知识库...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (localSearchQuery.isNotBlank()) onSemanticSearch(localSearchQuery)
                }) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Search, "语义搜索")
                    }
                }
            }

            // Semantic search results
            if (lastSemanticResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("语义搜索结果", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        lastSemanticResults.take(3).forEach { item ->
                            Text(
                                "· ${item.title}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                item.content.take(100) + "...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Items list
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.MenuBook, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("知识库为空", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("添加参考文本或手动创建知识条目", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                val displayList = if (searchResults.isNotEmpty()) searchResults else items
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(displayList, key = { it.id }) { item ->
                        KnowledgeCard(item = item, onDelete = { onDelete(item) })
                    }
                }
            }
        }

        // FAB
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(onClick = { showImportDialog = true }) {
                Icon(Icons.Filled.FileUpload, "导入文本")
            }
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, "添加知识")
            }
        }
    }

    if (showAddDialog) {
        KnowledgeEditDialog(
            title = "添加知识条目",
            onDismiss = { showAddDialog = false },
            onSave = { title, content ->
                onCreateItem(title, content, "manual")
                showAddDialog = false
            }
        )
    }

    if (showImportDialog) {
        ImportTextDialog(
            onDismiss = { showImportDialog = false },
            onImport = { title, text ->
                onImportText(title, text)
                showImportDialog = false
            }
        )
    }
}

@Composable
private fun KnowledgeCard(
    item: KnowledgeItemEntity,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    when (item.source) {
                                        "imported" -> "导入"
                                        "analysis" -> "分析"
                                        else -> "手动"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(item.content, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun KnowledgeEditDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("标题") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("内容") }, minLines = 5, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim(), content.trim()) }, enabled = name.isNotBlank()) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun ImportTextDialog(
    onDismiss: () -> Unit,
    onImport: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入参考文本") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("来源名称（如：参考作品名）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("文本内容") }, minLines = 6, modifier = Modifier.fillMaxWidth(), placeholder = { Text("粘贴参考文本，系统会自动分段...") })
                Text("文本将自动拆分为 500 字片段，便于精准检索", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(onClick = { onImport(title.trim(), text.trim()) }, enabled = title.isNotBlank() && text.isNotBlank()) { Text("导入") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
