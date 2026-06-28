package com.aiwrite.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiwrite.data.local.SettingsDataStore
import com.aiwrite.data.remote.DownloadProgress
import com.aiwrite.data.remote.ModelDownloadManager
import com.aiwrite.domain.model.AVAILABLE_MODELS
import com.aiwrite.domain.model.LocalLlmConfig
import com.aiwrite.domain.model.ModelInfo
import com.aiwrite.ml.MnnLlmEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalLlmViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val downloadManager: ModelDownloadManager,
    private val llmEngine: MnnLlmEngine
) : ViewModel() {

    private val _config = MutableStateFlow(LocalLlmConfig())
    val config: StateFlow<LocalLlmConfig> = _config

    val downloadProgress: StateFlow<DownloadProgress> = downloadManager.downloadProgress

    val availableModels = AVAILABLE_MODELS

    val downloadedModels: StateFlow<List<String>> = flow {
        while (true) {
            emit(downloadManager.getDownloadedModels().map { it.name })
            kotlinx.coroutines.delay(2000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _engineStatus = MutableStateFlow("未加载")
    val engineStatus: StateFlow<String> = _engineStatus

    fun updateConfig(config: LocalLlmConfig) {
        _config.value = config
    }

    fun toggleEnabled(enabled: Boolean) {
        _config.update { it.copy(isEnabled = enabled) }
    }

    fun downloadModel(model: ModelInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadManager.downloadModel(model.name, model.downloadUrl).fold(
                onSuccess = { file ->
                    _config.update { it.copy(modelPath = file.absolutePath, modelName = model.name) }
                },
                onFailure = { }
            )
        }
    }

    fun selectModel(path: String) {
        _config.update { it.copy(modelPath = path, modelName = path.substringAfterLast('/')) }
    }

    fun initializeEngine() {
        viewModelScope.launch(Dispatchers.IO) {
            _engineStatus.value = "加载中..."
            val ok = llmEngine.initialize(_config.value)
            _engineStatus.value = if (ok) "已就绪" else "加载失败"
        }
    }
}
