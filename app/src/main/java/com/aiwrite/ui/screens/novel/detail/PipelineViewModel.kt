package com.aiwrite.ui.screens.novel.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiwrite.data.repository.PipelineRepository
import com.aiwrite.domain.model.PipelineProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PipelineViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pipelineRepository: PipelineRepository
) : ViewModel() {

    private val novelId: String = savedStateHandle["novelId"]!!

    val progress: StateFlow<PipelineProgress> = pipelineRepository.progress

    init {
        viewModelScope.launch(Dispatchers.IO) {
            pipelineRepository.prepareJobs(novelId)
        }
    }

    fun startPipeline() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = progress.value
            if (current.jobs.isEmpty()) {
                pipelineRepository.prepareJobs(novelId)
            }
            pipelineRepository.runPipeline(progress.value)
        }
    }

    fun retryFailed() {
        pipelineRepository.retryFailed()
        viewModelScope.launch(Dispatchers.IO) {
            pipelineRepository.runPipeline(progress.value)
        }
    }

    fun stop() {
        pipelineRepository.stop()
    }
}
