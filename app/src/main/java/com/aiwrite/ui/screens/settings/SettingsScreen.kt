package com.aiwrite.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    localLlmViewModel: LocalLlmViewModel = hiltViewModel()
) {
    val config by viewModel.config.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Provider section
            Text("API 提供商", style = MaterialTheme.typography.titleMedium)

            var provider by remember { mutableStateOf("custom") }
            val providers = listOf("openai" to "OpenAI", "deepseek" to "DeepSeek", "siliconflow" to "SiliconFlow", "custom" to "自定义")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                providers.forEach { (key, label) ->
                    FilterChip(
                        selected = provider == key,
                        onClick = {
                            provider = key
                            when (key) {
                                "openai" -> {
                                    viewModel.updateBaseUrl("https://api.openai.com")
                                    viewModel.updatePlanningModel("gpt-4o")
                                    viewModel.updateWritingModel("gpt-4o")
                                }
                                "deepseek" -> {
                                    viewModel.updateBaseUrl("https://api.deepseek.com")
                                    viewModel.updatePlanningModel("deepseek-chat")
                                    viewModel.updateWritingModel("deepseek-chat")
                                }
                                "siliconflow" -> {
                                    viewModel.updateBaseUrl("https://api.siliconflow.cn")
                                }
                            }
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            OutlinedTextField(
                value = config.baseUrl,
                onValueChange = { viewModel.updateBaseUrl(it) },
                label = { Text("Base URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            var showKey by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = config.apiKey,
                onValueChange = { viewModel.updateApiKey(it) },
                label = { Text("API Key") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "显示/隐藏"
                        )
                    }
                }
            )

            HorizontalDivider()

            // Model selection
            Text("模型配置", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = config.planningModel,
                onValueChange = { viewModel.updatePlanningModel(it) },
                label = { Text("规划模型") },
                supportingText = { Text("用于开书策划、世界观、角色设计") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = config.writingModel,
                onValueChange = { viewModel.updateWritingModel(it) },
                label = { Text("写作模型") },
                supportingText = { Text("用于正文生成、续写") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = config.reviewModel,
                onValueChange = { viewModel.updateReviewModel(it) },
                label = { Text("审阅模型") },
                supportingText = { Text("用于审阅、修改建议，可用较小模型") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // Parameters
            Text("生成参数", style = MaterialTheme.typography.titleMedium)

            var maxTokensText by remember { mutableStateOf(config.maxTokens.toString()) }
            OutlinedTextField(
                value = maxTokensText,
                onValueChange = {
                    maxTokensText = it
                    it.toIntOrNull()?.let { tokens -> viewModel.updateMaxTokens(tokens) }
                },
                label = { Text("最大 Token 数") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Temperature: ${config.temperature}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = config.temperature,
                onValueChange = { viewModel.updateTemperature(it) },
                valueRange = 0f..2f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0.0 精确", style = MaterialTheme.typography.labelSmall)
                Text("2.0 创意", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            // Local LLM
            val llmConfig by localLlmViewModel.config.collectAsState()
            val downloadProgress by localLlmViewModel.downloadProgress.collectAsState()
            val downloadedModels by localLlmViewModel.downloadedModels.collectAsState()
            val engineStatus by localLlmViewModel.engineStatus.collectAsState()

            LocalLlmPanel(
                config = llmConfig,
                downloadProgress = downloadProgress,
                downloadedModels = downloadedModels,
                engineStatus = engineStatus,
                onToggleEnabled = { localLlmViewModel.toggleEnabled(it) },
                onDownloadModel = { localLlmViewModel.downloadModel(it) },
                onSelectModel = { localLlmViewModel.selectModel(it) },
                onInitialize = { localLlmViewModel.initializeEngine() }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
