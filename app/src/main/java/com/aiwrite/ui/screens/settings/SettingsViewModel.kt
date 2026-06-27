package com.aiwrite.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiwrite.data.local.SettingsDataStore
import com.aiwrite.domain.model.ApiConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val config: StateFlow<ApiConfig> = settingsDataStore.apiConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiConfig())

    fun updateBaseUrl(url: String) {
        viewModelScope.launch { settingsDataStore.updateBaseUrl(url) }
    }

    fun updateApiKey(key: String) {
        viewModelScope.launch { settingsDataStore.updateApiKey(key) }
    }

    fun updatePlanningModel(model: String) {
        viewModelScope.launch { settingsDataStore.updatePlanningModel(model) }
    }

    fun updateWritingModel(model: String) {
        viewModelScope.launch { settingsDataStore.updateWritingModel(model) }
    }

    fun updateReviewModel(model: String) {
        viewModelScope.launch { settingsDataStore.updateReviewModel(model) }
    }

    fun updateMaxTokens(tokens: Int) {
        viewModelScope.launch { settingsDataStore.updateMaxTokens(tokens) }
    }

    fun updateTemperature(temp: Float) {
        viewModelScope.launch { settingsDataStore.updateTemperature(temp) }
    }
}
