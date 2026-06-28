package com.aiwrite.ui.screens.settings

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
import com.aiwrite.domain.model.AVAILABLE_MODELS
import com.aiwrite.domain.model.LocalLlmConfig
import com.aiwrite.domain.model.ModelInfo

@Composable
fun LocalLlmPanel(
    config: LocalLlmConfig,
    downloadProgress: com.aiwrite.data.remote.DownloadProgress,
    downloadedModels: List<String>,
    engineStatus: String,
    onToggleEnabled: (Boolean) -> Unit,
    onDownloadModel: (ModelInfo) -> Unit,
    onSelectModel: (String) -> Unit,
    onInitialize: () -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Enable toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("启用本地模型", style = MaterialTheme.typography.titleMedium)
                Text("下载并运行本地 LLM，无需网络连接", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = config.isEnabled, onCheckedChange = onToggleEnabled)
        }

        if (config.isEnabled) {
            HorizontalDivider()

            // Engine status
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusColor = when (engineStatus) {
                    "已就绪" -> MaterialTheme.colorScheme.primary
                    "加载中..." -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = statusColor,
                    modifier = Modifier.size(8.dp)
                ) { }
                Spacer(modifier = Modifier.width(8.dp))
                Text("引擎状态: $engineStatus", style = MaterialTheme.typography.bodyMedium)
            }

            // Download models
            Text("下载模型", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

            if (downloadProgress.isDownloading) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("下载中: ${downloadProgress.modelName}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(progress = { downloadProgress.progress }, modifier = Modifier.fillMaxWidth())
                        Text(
                            "${downloadProgress.downloadedBytes / (1024 * 1024)}MB / ${downloadProgress.totalBytes / (1024 * 1024)}MB",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            if (downloadProgress.error != null) {
                Text("错误: ${downloadProgress.error}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            AVAILABLE_MODELS.forEach { model ->
                val isDownloaded = downloadedModels.any { it == model.name }
                val isDownloading = downloadProgress.isDownloading && downloadProgress.modelName == model.name

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(model.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("${model.size}  ${model.description}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (isDownloaded) {
                            Button(
                                onClick = { onSelectModel(model.name) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) { Text("使用") }
                        } else {
                            FilledTonalButton(
                                onClick = { onDownloadModel(model) },
                                enabled = !isDownloading,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) { Text(if (isDownloading) "下载中" else "下载") }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Initialize button
            if (config.modelPath.isNotBlank()) {
                OutlinedButton(onClick = onInitialize, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("初始化引擎")
                }
            }

            Text(
                "提示：首次使用需下载约 1GB 模型文件，建议在 WiFi 环境下进行。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
