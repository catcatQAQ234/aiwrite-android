package com.aiwrite.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aiwrite.data.local.SettingsDataStore
import com.aiwrite.data.local.entity.NovelEntity

/**
 * Shared novel selector bar, used across screens.
 * Displays current novel and allows switching.
 */
@Composable
fun NovelContextBar(
    novels: List<NovelEntity>,
    selectedNovelId: String?,
    selectedNovelTitle: String?,
    onSelectNovel: (NovelEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.MenuBook, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Text("当前：", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Box {
            TextButton(
                onClick = { expanded = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    selectedNovelTitle ?: "选择小说",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Icon(Icons.Filled.ArrowDropDown, null, modifier = Modifier.size(18.dp))
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                novels.forEach { novel ->
                    DropdownMenuItem(
                        text = { Text(novel.title, fontWeight = if (novel.id == selectedNovelId) FontWeight.Bold else FontWeight.Normal) },
                        onClick = {
                            onSelectNovel(novel)
                            expanded = false
                        },
                        leadingIcon = if (novel.id == selectedNovelId) {
                            { Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
                if (novels.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("请先去「小说」创建项目") },
                        onClick = { expanded = false },
                        enabled = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
    HorizontalDivider()
}
