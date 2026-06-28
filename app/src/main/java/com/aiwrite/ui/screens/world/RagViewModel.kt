package com.aiwrite.ui.screens.world

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiwrite.data.local.entity.KnowledgeItemEntity
import com.aiwrite.data.repository.RagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RagViewModel @Inject constructor(
    private val ragRepository: RagRepository
) : ViewModel() {

    private val _novelId = MutableStateFlow<String?>(null)

    val items: StateFlow<List<KnowledgeItemEntity>> = _novelId
        .flatMapLatest { id ->
            if (id != null) ragRepository.getByNovel(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<KnowledgeItemEntity>>(emptyList())
    val searchResults: StateFlow<List<KnowledgeItemEntity>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _lastSemanticResults = MutableStateFlow<List<KnowledgeItemEntity>>(emptyList())
    val lastSemanticResults: StateFlow<List<KnowledgeItemEntity>> = _lastSemanticResults

    fun setNovelId(id: String?) {
        _novelId.value = id
    }

    fun createItem(title: String, content: String, source: String = "manual") {
        val novelId = _novelId.value ?: return
        viewModelScope.launch {
            ragRepository.create(novelId, title, content, source)
        }
    }

    fun importText(title: String, text: String) {
        val novelId = _novelId.value ?: return
        viewModelScope.launch {
            ragRepository.importText(novelId, title, text)
        }
    }

    fun deleteItem(item: KnowledgeItemEntity) {
        viewModelScope.launch {
            ragRepository.delete(item)
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        val novelId = _novelId.value ?: return
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            ragRepository.search(novelId, query).first().let {
                _searchResults.value = it
            }
        }
    }

    fun semanticSearch(query: String) {
        val novelId = _novelId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isSearching.value = true
            try {
                val results = ragRepository.semanticSearch(novelId, query, 5)
                _lastSemanticResults.value = results
            } catch (_: Exception) { }
            _isSearching.value = false
        }
    }
}
