package com.aiwrite.ui.screens.novel.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiwrite.domain.model.JobStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipelineScreen(
    onBack: () -> Unit,
    viewModel: PipelineViewModel = hiltViewModel()
) {
    val progress by viewModel.progress.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("整本生产") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text("《${progress.novelTitle}》", style = MaterialTheme.typography.headlineSmall)
            Text(
                "批量生成所有章节正文",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Progress
            if (progress.totalChapters > 0) {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("生成进度", fontWeight = FontWeight.Bold)
                            Text("${progress.completedChapters}/${progress.totalChapters}")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${(progress.progress * 100).toInt()}% 完成",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.startPipeline() },
                    enabled = !progress.isRunning && progress.jobs.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("开始批量生成")
                }
                if (progress.isRunning) {
                    OutlinedButton(
                        onClick = { viewModel.stop() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Stop, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("停止")
                    }
                }
            }

            // Failed retry
            val failedCount = progress.jobs.count { it.status == JobStatus.FAILED }
            if (failedCount > 0 && !progress.isRunning) {
                OutlinedButton(
                    onClick = { viewModel.retryFailed() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("重试 $failedCount 个失败章节")
                }
            }

            // Job list
            if (progress.jobs.isNotEmpty()) {
                Text("章节列表", style = MaterialTheme.typography.titleSmall)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(progress.jobs) { index, job ->
                        val isCurrent = index == progress.currentJobIndex && progress.isRunning
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isCurrent -> MaterialTheme.colorScheme.tertiaryContainer
                                    job.status == JobStatus.DONE -> MaterialTheme.colorScheme.surfaceVariant
                                    job.status == JobStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status icon
                                Icon(
                                    imageVector = when (job.status) {
                                        JobStatus.PENDING -> Icons.Filled.Circle
                                        JobStatus.GENERATING -> Icons.Filled.Sync
                                        JobStatus.DONE -> Icons.Filled.CheckCircle
                                        JobStatus.FAILED -> Icons.Filled.Error
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = when (job.status) {
                                        JobStatus.DONE -> MaterialTheme.colorScheme.primary
                                        JobStatus.FAILED -> MaterialTheme.colorScheme.error
                                        JobStatus.GENERATING -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        job.chapterTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        job.volumeTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (job.error != null) {
                                        Text(
                                            job.error,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
