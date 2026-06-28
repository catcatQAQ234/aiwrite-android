package com.aiwrite.ui.screens.imagegen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiwrite.data.local.entity.GenerationHistoryEntity
import com.aiwrite.data.repository.ImageRepository
import com.aiwrite.domain.model.ImageGenParams
import com.aiwrite.domain.model.SCHEDULERS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageGenViewModel @Inject constructor(
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _params = MutableStateFlow(ImageGenParams())
    val params: StateFlow<ImageGenParams> = _params

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _engineReady = MutableStateFlow(false)
    val engineReady: StateFlow<Boolean> = _engineReady

    val history: StateFlow<List<GenerationHistoryEntity>> = imageRepository.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<GenerationHistoryEntity>> = imageRepository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _engineReady.value = imageRepository.initializeEngine()
        }
    }

    fun updatePrompt(prompt: String) { _params.update { it.copy(prompt = prompt) } }
    fun updateNegativePrompt(np: String) { _params.update { it.copy(negativePrompt = np) } }
    fun updateWidth(w: Int) { _params.update { it.copy(width = w) } }
    fun updateHeight(h: Int) { _params.update { it.copy(height = h) } }
    fun updateSteps(s: Int) { _params.update { it.copy(steps = s) } }
    fun updateCfgScale(cfg: Float) { _params.update { it.copy(cfgScale = cfg) } }
    fun updateSeed(seed: Long) { _params.update { it.copy(seed = seed) } }
    fun updateScheduler(s: String) { _params.update { it.copy(scheduler = s) } }
    fun updateDenoiseStrength(d: Float) { _params.update { it.copy(denoiseStrength = d) } }
    fun toggleCpu() { _params.update { it.copy(useCpu = !it.useCpu) } }

    fun generate() {
        if (_params.value.prompt.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            try {
                imageRepository.generate(_params.value)
            } catch (_: Exception) { }
            _isGenerating.value = false
        }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch { imageRepository.toggleFavorite(id) }
    }

    fun deleteHistoryItem(item: GenerationHistoryEntity) {
        viewModelScope.launch { imageRepository.deleteHistory(item) }
    }

    fun saveToGallery(imagePath: String) {
        viewModelScope.launch { imageRepository.saveToGallery(imagePath) }
    }

    fun randomSeed() {
        _params.update { it.copy(seed = System.currentTimeMillis()) }
    }
}
