package com.greatnotes.presentation.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.greatnotes.R
import com.greatnotes.domain.model.CoverStyle
import com.greatnotes.domain.model.Notebook
import com.greatnotes.domain.model.NotebookCover
import com.greatnotes.domain.model.PageTemplate
import com.greatnotes.presentation.library.components.CreateNotebookSheet
import com.greatnotes.presentation.library.components.EmptyLibrary
import com.greatnotes.presentation.library.components.NotebookShelf
import com.greatnotes.presentation.theme.GreatNotesTheme

/** Stateful container: owns the ViewModel and lifecycle-aware collection. */
@Composable
fun LibraryScreen(
    onNotebookClick: (notebookId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LibraryContent(
        state = state,
        onNotebookClick = onNotebookClick,
        onCreateNotebook = viewModel::createNotebook,
        onErrorShown = viewModel::onErrorShown,
        modifier = modifier,
    )
}

/** Stateless presentation of the shelf. Only transient UI state (sheet visibility) lives here. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryContent(
    state: LibraryUiState,
    onNotebookClick: (String) -> Unit,
    onCreateNotebook: (String, NotebookCover, PageTemplate) -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMessage = state.error?.let { stringResource(it.messageRes) }
    LaunchedEffect(state.error) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(R.string.library_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.notebooks.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateSheet = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.library_new_notebook)) },
                )
            }
        },
    ) { padding ->
        val contentModifier = Modifier.fillMaxSize().padding(padding)
        when {
            state.isLoading && state.notebooks.isEmpty() -> {
                val loadingDescription = stringResource(R.string.library_loading)
                Box(contentModifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.semantics { contentDescription = loadingDescription })
                }
            }
            state.notebooks.isEmpty() -> EmptyLibrary(
                onCreateClick = { showCreateSheet = true },
                modifier = contentModifier,
            )
            else -> NotebookShelf(
                notebooks = state.notebooks,
                onNotebookClick = onNotebookClick,
                modifier = contentModifier,
            )
        }
    }

    if (showCreateSheet) {
        CreateNotebookSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { title, cover, template ->
                showCreateSheet = false
                onCreateNotebook(title, cover, template)
            },
        )
    }
}

private val PreviewNotebooks = listOf(
    "Physics" to CoverStyle.LEATHER, "Journal" to CoverStyle.LINEN,
    "Sketches" to CoverStyle.GRADIENT, "Ideas" to CoverStyle.PLAIN,
    "Math" to CoverStyle.LEATHER, "Recipes" to CoverStyle.LINEN,
).mapIndexed { i, (title, style) ->
    Notebook(
        id = "$i", title = title,
        cover = NotebookCover(style, listOf(0xFF4E342E, 0xFF2E7D32, 0xFF6A1B9A, 0xFF2962FF)[i % 4].toInt()),
        createdAt = 0, updatedAt = 0, pageCount = i * 3 + 1, isArchived = false, sortIndex = i,
    )
}

@Preview(name = "Tablet landscape", widthDp = 1280, heightDp = 800)
@Composable
private fun LibraryContentTabletPreview() {
    GreatNotesTheme(dynamicColor = false) {
        LibraryContent(LibraryUiState(false, PreviewNotebooks), {}, { _, _, _ -> }, {})
    }
}

@Preview(name = "Empty", widthDp = 800, heightDp = 1280)
@Composable
private fun LibraryContentEmptyPreview() {
    GreatNotesTheme(dynamicColor = false) {
        LibraryContent(LibraryUiState(isLoading = false), {}, { _, _, _ -> }, {})
    }
}

@Preview(name = "Loading", widthDp = 400, heightDp = 700)
@Composable
private fun LibraryContentLoadingPreview() {
    GreatNotesTheme(dynamicColor = false) {
        LibraryContent(LibraryUiState(), {}, { _, _, _ -> }, {})
    }
}
