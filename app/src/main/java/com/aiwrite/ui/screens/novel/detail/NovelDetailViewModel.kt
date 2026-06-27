package com.aiwrite.ui.screens.novel.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiwrite.data.local.entity.ChapterEntity
import com.aiwrite.data.local.entity.NovelEntity
import com.aiwrite.data.local.entity.VolumeEntity
import com.aiwrite.data.repository.NovelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class NovelDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NovelRepository
) : ViewModel() {

    private val novelId: String = savedStateHandle["novelId"]!!

    val novel: StateFlow<NovelEntity?> = repository.getAllNovels()
        .flatMapLatest { novels ->
            val found = novels.find { it.id == novelId }
            MutableStateFlow(found).also { it.value = found }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val volumes: StateFlow<List<VolumeEntity>> = repository.getVolumesByNovel(novelId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createVolume(title: String) {
        viewModelScope.launch {
            repository.createVolume(novelId, title)
        }
    }

    fun deleteVolume(volume: VolumeEntity) {
        viewModelScope.launch {
            repository.deleteVolume(volume)
        }
    }

    fun createChapter(volumeId: String, title: String) {
        viewModelScope.launch {
            repository.createChapter(volumeId, title)
        }
    }

    fun deleteChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.deleteChapter(chapter)
        }
    }

    fun getChapters(volumeId: String) = repository.getChaptersByVolume(volumeId)
}
