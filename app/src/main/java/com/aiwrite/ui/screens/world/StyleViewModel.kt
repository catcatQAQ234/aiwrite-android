package com.aiwrite.ui.screens.world

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiwrite.data.local.entity.StyleProfileEntity
import com.aiwrite.data.repository.StyleRepository
import com.aiwrite.domain.model.StyleFeature
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StyleViewModel @Inject constructor(
    private val styleRepository: StyleRepository
) : ViewModel() {

    private val _novelId = MutableStateFlow<String?>(null)

    val profiles: StateFlow<List<StyleProfileEntity>> = _novelId
        .flatMapLatest { id ->
            if (id != null) styleRepository.getProfilesByNovel(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setNovelId(id: String?) {
        _novelId.value = id
    }

    fun createProfile(name: String, description: String = "") {
        val novelId = _novelId.value ?: return
        viewModelScope.launch {
            styleRepository.create(novelId, name, description)
        }
    }

    fun deleteProfile(profile: StyleProfileEntity) {
        viewModelScope.launch {
            styleRepository.delete(profile)
        }
    }

    fun setActive(profile: StyleProfileEntity) {
        viewModelScope.launch {
            styleRepository.setActive(profile)
        }
    }

    fun addFeature(profile: StyleProfileEntity, feature: StyleFeature) {
        viewModelScope.launch {
            styleRepository.addFeature(profile, feature)
        }
    }

    fun toggleFeature(profile: StyleProfileEntity, featureId: String, enabled: Boolean) {
        viewModelScope.launch {
            styleRepository.toggleFeature(profile, featureId, enabled)
        }
    }
}
