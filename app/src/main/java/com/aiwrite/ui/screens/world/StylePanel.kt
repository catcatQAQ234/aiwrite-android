package com.aiwrite.ui.screens.world

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import com.aiwrite.data.local.entity.StyleProfileEntity
import com.aiwrite.data.repository.StyleRepository
import com.aiwrite.data.repository.StyleRepository.Companion.parseFeatures
import com.aiwrite.domain.model.StyleFeature

@Composable
fun StylePanel(
    profiles: List<StyleProfileEntity>,
    onCreateProfile: (String, String) -> Unit,
    onDeleteProfile: (StyleProfileEntity) -> Unit,
    onSetActive: (StyleProfileEntity) -> Unit,
    onAddFeature: (StyleProfileEntity, StyleFeature) -> Unit,
    onToggleFeature: (StyleProfileEntity, String, Boolean) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedProfile by remember { mutableStateOf<StyleProfileEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (profiles.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.Style, null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("还没有写法模板", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("创建一个写法模板来定义写作风格", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    StyleProfileCard(
                        profile = profile,
                        isExpanded = selectedProfile?.id == profile.id,
                        onClick = {
                            selectedProfile = if (selectedProfile?.id == profile.id) null else profile
                        },
                        onSetActive = { onSetActive(profile) },
                        onDelete = { onDeleteProfile(profile) },
                        onAddFeature = { feature -> onAddFeature(profile, feature) },
                        onToggleFeature = { featureId, enabled -> onToggleFeature(profile, featureId, enabled) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, "创建写法")
        }
    }

    if (showCreateDialog) {
        CreateStyleDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc ->
                onCreateProfile(name, desc)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun StyleProfileCard(
    profile: StyleProfileEntity,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onSetActive: () -> Unit,
    onDelete: () -> Unit,
    onAddFeature: (StyleFeature) -> Unit,
    onToggleFeature: (String, Boolean) -> Unit
) {
    val features = remember(profile.features) { parseFeatures(profile.features) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = if (profile.isActive) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(profile.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        if (profile.isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            SuggestionChip(
                                onClick = {},
                                label = { Text("使用中", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    if (profile.description.isNotBlank()) {
                        Text(
                            profile.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    if (!profile.isActive) {
                        IconButton(onClick = onSetActive) {
                            Icon(Icons.Filled.CheckCircle, "启用", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    // Feature list
                    Text("特征池", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                    if (features.isEmpty()) {
                        Text(
                            "还没有添加特征，点击下方按钮添加",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        features.forEach { feature ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = feature.enabled,
                                    onCheckedChange = { onToggleFeature(feature.id, it) },
                                    modifier = Modifier.height(32.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(feature.name, style = MaterialTheme.typography.bodyMedium)
                                    if (feature.description.isNotBlank()) {
                                        Text(
                                            feature.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Add feature from templates
                    var showTemplateMenu by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { showTemplateMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("从模板添加特征")
                        }
                        DropdownMenu(
                            expanded = showTemplateMenu,
                            onDismissRequest = { showTemplateMenu = false }
                        ) {
                            StyleRepository.FEATURE_TEMPLATES.forEach { template ->
                                val alreadyAdded = features.any { it.name == template.name }
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(template.name)
                                            Text(
                                                template.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (!alreadyAdded) {
                                            onAddFeature(template)
                                        }
                                        showTemplateMenu = false
                                    },
                                    enabled = !alreadyAdded
                                )
                            }
                        }
                    }

                    // Compiled rules preview
                    if (profile.compiledRules.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("编译规则预览", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                profile.compiledRules,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateStyleDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建写法模板") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("模板名称") },
                    placeholder = { Text("如：冷峻写实风、古风诗意风") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name.trim(), description.trim()) },
                enabled = name.isNotBlank()
            ) { Text("创建") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
