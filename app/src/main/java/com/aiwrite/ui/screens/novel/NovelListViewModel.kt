package com.aiwrite.ui.screens.novel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiwrite.data.local.entity.NovelEntity
import com.aiwrite.data.repository.NovelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NovelListViewModel @Inject constructor(
    private val repository: NovelRepository
) : ViewModel() {

    val novels: StateFlow<List<NovelEntity>> = repository.getAllNovels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createNovel(title: String, synopsis: String) {
        viewModelScope.launch {
            repository.createNovel(title, synopsis)
        }
    }

    fun deleteNovel(novel: NovelEntity) {
        viewModelScope.launch {
            repository.deleteNovel(novel)
        }
    }
}
