package com.aiwrite.ui.screens.world

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiwrite.data.local.entity.CharacterProfileEntity
import com.aiwrite.data.local.entity.NovelEntity
import com.aiwrite.data.local.entity.WorldSettingEntity
import com.aiwrite.data.repository.CharacterRepository
import com.aiwrite.data.repository.NovelRepository
import com.aiwrite.data.repository.WorldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorldViewModel @Inject constructor(
    private val novelRepository: NovelRepository,
    private val worldRepository: WorldRepository,
    private val characterRepository: CharacterRepository
) : ViewModel() {

    val novels: StateFlow<List<NovelEntity>> = novelRepository.getAllNovels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedNovelId = MutableStateFlow<String?>(null)

    val worldSettings: StateFlow<List<WorldSettingEntity>> = _selectedNovelId
        .flatMapLatest { id ->
            if (id != null) worldRepository.getSettingsByNovel(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val characters: StateFlow<List<CharacterProfileEntity>> = _selectedNovelId
        .flatMapLatest { id ->
            if (id != null) characterRepository.getCharactersByNovel(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedNovel: StateFlow<NovelEntity?> = combine(novels, _selectedNovelId) { list, id ->
        list.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectNovel(novelId: String?) {
        _selectedNovelId.value = novelId
    }

    // World settings
    fun createWorldSetting(name: String, category: String, content: String = "") {
        val novelId = _selectedNovelId.value ?: return
        viewModelScope.launch {
            worldRepository.create(novelId, name, category, content)
        }
    }

    fun updateWorldSetting(entity: WorldSettingEntity) {
        viewModelScope.launch {
            worldRepository.update(entity)
        }
    }

    fun deleteWorldSetting(entity: WorldSettingEntity) {
        viewModelScope.launch {
            worldRepository.delete(entity)
        }
    }

    // Characters
    fun createCharacter(name: String, role: String = "") {
        val novelId = _selectedNovelId.value ?: return
        viewModelScope.launch {
            characterRepository.create(novelId, name, role)
        }
    }

    fun updateCharacter(entity: CharacterProfileEntity) {
        viewModelScope.launch {
            characterRepository.update(entity)
        }
    }

    fun deleteCharacter(entity: CharacterProfileEntity) {
        viewModelScope.launch {
            characterRepository.delete(entity)
        }
    }
}
