package com.greatnotes.presentation.library

import androidx.annotation.StringRes
import com.greatnotes.R
import com.greatnotes.domain.model.Notebook

/** Transient, user-visible failures. Mapped to string resources so the ViewModel stays Context-free. */
enum class LibraryError(@param:StringRes val messageRes: Int) {
    LOAD_FAILED(R.string.library_error_load),
    CREATE_FAILED(R.string.library_error_create),
}

/** [isLoading] with a non-empty [notebooks] list means "refreshing with stale data visible". */
data class LibraryUiState(
    val isLoading: Boolean = true,
    val notebooks: List<Notebook> = emptyList(),
    val error: LibraryError? = null,
)
