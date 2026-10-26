package com.greatnotes.presentation.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.greatnotes.domain.model.Notebook

/**
 * Adaptive shelf of notebook covers.
 *
 * [MIN_CELL_WIDTH] is sized so a cover stays readable on a large tablet rather than for maximum
 * density. At 16dp gutters that yields roughly 5-6 columns on a ~1280dp landscape tablet, 3-4 on
 * the same device in portrait, and 2 on a phone. The exact count is resolved at layout time by
 * [GridCells.Adaptive]; those figures are estimates and have not been measured on a device.
 */
@Composable
fun NotebookShelf(
    notebooks: List<Notebook>,
    onNotebookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val layoutDirection = LocalLayoutDirection.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = MIN_CELL_WIDTH),
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp + contentPadding.calculateStartPadding(layoutDirection),
            top = 16.dp + contentPadding.calculateTopPadding(),
            end = 16.dp + contentPadding.calculateEndPadding(layoutDirection),
            // Extra room so the FAB never covers the last row.
            bottom = 96.dp + contentPadding.calculateBottomPadding(),
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(items = notebooks, key = { it.id }) { notebook ->
            NotebookCard(notebook = notebook, onClick = { onNotebookClick(notebook.id) })
        }
    }
}

private val MIN_CELL_WIDTH = 200.dp
