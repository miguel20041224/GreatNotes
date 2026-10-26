package com.greatnotes.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greatnotes.domain.model.Notebook
import com.greatnotes.domain.model.NotebookCover
import com.greatnotes.domain.model.PageTemplate
import com.greatnotes.domain.usecase.CreateNotebookWithFirstPageUseCase
import com.greatnotes.domain.usecase.ObserveNotebooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    observeNotebooks: ObserveNotebooksUseCase,
    private val createNotebook: CreateNotebookWithFirstPageUseCase,
) : ViewModel() {

    private val error = MutableStateFlow<LibraryError?>(null)

    val uiState: StateFlow<LibraryUiState> = combine(
        observeNotebooks().catch { error.value = LibraryError.LOAD_FAILED; emit(emptyList<Notebook>()) },
        error,
    ) { notebooks, err ->
        LibraryUiState(isLoading = false, notebooks = notebooks, error = err)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    /**
     * Creates the notebook and its first page in one guaranteed step: on failure nothing is left
     * on the shelf and [LibraryError.CREATE_FAILED] is surfaced.
     */
    fun createNotebook(title: String, cover: NotebookCover, template: PageTemplate) {
        viewModelScope.launch {
            try {
                createNotebook(title, cover, template)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error.value = LibraryError.CREATE_FAILED
            }
        }
    }

    fun onErrorShown() {
        error.value = null
    }
}
