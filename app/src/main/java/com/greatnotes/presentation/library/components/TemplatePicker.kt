package com.greatnotes.presentation.library.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.greatnotes.R
import com.greatnotes.domain.model.PageTemplate
import com.greatnotes.presentation.theme.GreatNotesTheme

@StringRes
private fun PageTemplate.labelRes(): Int = when (this) {
    PageTemplate.BLANK -> R.string.template_blank
    PageTemplate.LINED -> R.string.template_lined
    PageTemplate.GRID -> R.string.template_grid
    PageTemplate.DOTTED -> R.string.template_dotted
}

@Composable
fun TemplatePicker(
    selected: PageTemplate,
    onSelected: (PageTemplate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(R.string.create_sheet_paper_section)
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PageTemplate.entries.forEach { template ->
                PickerOption(
                    selected = template == selected,
                    label = stringResource(template.labelRes()),
                    onClick = { onSelected(template) },
                ) { PaperPreview(template) }
            }
        }
    }
}

/** Miniature, purely decorative paper sample. */
@Composable
private fun PaperPreview(template: PageTemplate) {
    val ink = MaterialTheme.colorScheme.outline
    Canvas(
        Modifier
            .size(width = 48.dp, height = 64.dp)
            .background(Color.White)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        val step = 8.dp.toPx()
        when (template) {
            PageTemplate.BLANK -> Unit
            PageTemplate.LINED -> {
                var y = step
                while (y < size.height) {
                    drawLine(ink, Offset(0f, y), Offset(size.width, y), 1f)
                    y += step
                }
            }
            PageTemplate.GRID -> {
                var y = step
                while (y < size.height) { drawLine(ink, Offset(0f, y), Offset(size.width, y), 1f); y += step }
                var x = step
                while (x < size.width) { drawLine(ink, Offset(x, 0f), Offset(x, size.height), 1f); x += step }
            }
            PageTemplate.DOTTED -> {
                var y = step
                while (y < size.height) {
                    var x = step
                    while (x < size.width) { drawCircle(ink, 1.2.dp.toPx(), Offset(x, y)); x += step }
                    y += step
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TemplatePickerPreview() {
    GreatNotesTheme(dynamicColor = false) {
        TemplatePicker(PageTemplate.LINED, onSelected = {}, modifier = Modifier.padding(16.dp))
    }
}
