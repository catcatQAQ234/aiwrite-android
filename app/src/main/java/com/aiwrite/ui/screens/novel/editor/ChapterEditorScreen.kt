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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterEditorScreen(
    onBack: () -> Unit,
    viewModel: ChapterEditorViewModel = hiltViewModel()
) {
    val chapter by viewModel.chapter.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(chapter?.title ?: "章节编辑")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.save()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.save() }) {
                        Icon(
                            Icons.Filled.Save,
                            contentDescription = "保存",
                            tint = if (isSaved) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            OutlinedTextField(
                value = chapter?.title ?: "",
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("章节标题") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge
            )

            // Outline
            OutlinedTextField(
                value = chapter?.outline ?: "",
                onValueChange = { viewModel.updateOutline(it) },
                label = { Text("章节大纲") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            // Content
            OutlinedTextField(
                value = chapter?.content ?: "",
                onValueChange = { viewModel.updateContent(it) },
                label = { Text("正文内容") },
                minLines = 20,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
