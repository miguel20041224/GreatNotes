package com.greatnotes.presentation.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.greatnotes.R
import com.greatnotes.domain.model.CoverStyle
import com.greatnotes.domain.model.Notebook
import com.greatnotes.domain.model.NotebookCover
import com.greatnotes.domain.model.PageTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNotebookSheet(
    onDismiss: () -> Unit,
    onCreate: (title: String, cover: NotebookCover, template: PageTemplate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by rememberSaveable { mutableStateOf("") }
    var touched by rememberSaveable { mutableStateOf(false) }
    var style by rememberSaveable { mutableStateOf(CoverStyle.PLAIN) }
    var colorArgb by rememberSaveable { mutableIntStateOf(NotebookCover.DEFAULT.colorArgb) }
    var template by rememberSaveable { mutableStateOf(PageTemplate.BLANK) }

    val trimmed = title.trim()
    val isBlank = trimmed.isEmpty()
    val isTooLong = trimmed.length > Notebook.MAX_TITLE_LENGTH
    val canSubmit = !isBlank && !isTooLong
    val showError = isTooLong || (touched && isBlank)

    fun submit() {
        touched = true
        if (canSubmit) onCreate(trimmed, NotebookCover(style, colorArgb), template)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, modifier = modifier) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(stringResource(R.string.create_sheet_title), style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; touched = true },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_sheet_name_label)) },
                singleLine = true,
                isError = showError,
                supportingText = {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            when {
                                isTooLong -> stringResource(R.string.create_sheet_name_too_long)
                                showError -> stringResource(R.string.create_sheet_name_blank)
                                else -> ""
                            },
                        )
                        Text(
                            stringResource(
                                R.string.create_sheet_name_counter,
                                trimmed.length,
                                Notebook.MAX_TITLE_LENGTH,
                            ),
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )

            CoverPicker(
                cover = NotebookCover(style, colorArgb),
                onCoverChange = { style = it.style; colorArgb = it.colorArgb },
            )
            TemplatePicker(selected = template, onSelected = { template = it })

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End),
            ) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.create_sheet_cancel)) }
                Button(onClick = ::submit, enabled = canSubmit) {
                    Text(stringResource(R.string.create_sheet_confirm))
                }
            }
        }
    }
}
