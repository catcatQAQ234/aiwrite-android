package com.aiwrite.ui.screens.novel.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterEditorScreen(
    onBack: () -> Unit,
    viewModel: ChapterEditorViewModel = hiltViewModel()
) {
    val chapter by viewModel.chapter.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Auto-save: debounce 3s after last edit
    LaunchedEffect(isSaved) {
        if (!isSaved) {
            delay(3000)
            viewModel.save()
            scope.launch { snackbarHostState.showSnackbar("已自动保存") }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(chapter?.title ?: "章节编辑", style = MaterialTheme.typography.titleMedium)
                        if (chapter != null) {
                            Text(
                                if (isSaved) "已保存" else "未保存",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.save()
                        onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.save()
                        scope.launch { snackbarHostState.showSnackbar("已手动保存") }
                    }) {
                        Icon(Icons.Filled.Save, "保存", tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = chapter?.title ?: "",
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("章节标题") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = chapter?.outline ?: "",
                onValueChange = { viewModel.updateOutline(it) },
                label = { Text("章节大纲") },
                minLines = 2, maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            Text("正文", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = chapter?.content ?: "",
                onValueChange = { viewModel.updateContent(it) },
                placeholder = { Text("开始写作...") },
                minLines = 20,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            // Word count
            val wordCount = chapter?.content?.length ?: 0
            Text(
                "字数: $wordCount",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
