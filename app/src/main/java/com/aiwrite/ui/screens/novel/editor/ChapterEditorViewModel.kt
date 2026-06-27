package com.aiwrite.ui.screens.novel.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiwrite.data.local.entity.ChapterEntity
import com.aiwrite.data.repository.NovelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChapterEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NovelRepository
) : ViewModel() {

    private val chapterId: String = savedStateHandle["chapterId"]!!

    private val _chapter = MutableStateFlow<ChapterEntity?>(null)
    val chapter: StateFlow<ChapterEntity?> = _chapter

    private val _isSaved = MutableStateFlow(true)
    val isSaved: StateFlow<Boolean> = _isSaved

    init {
        viewModelScope.launch {
            val ch = repository.getChapterById(chapterId)
            _chapter.value = ch
        }
    }

    fun updateContent(content: String) {
        _chapter.value = _chapter.value?.copy(content = content)
        _isSaved.value = false
    }

    fun updateTitle(title: String) {
        _chapter.value = _chapter.value?.copy(title = title)
        _isSaved.value = false
    }

    fun updateOutline(outline: String) {
        _chapter.value = _chapter.value?.copy(outline = outline)
        _isSaved.value = false
    }

    fun save() {
        viewModelScope.launch {
            _chapter.value?.let { repository.updateChapter(it) }
            _isSaved.value = true
        }
    }
}
