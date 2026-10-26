package com.greatnotes.presentation.library.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.greatnotes.R
import com.greatnotes.domain.model.CoverStyle
import com.greatnotes.domain.model.NotebookCover
import com.greatnotes.presentation.theme.GreatNotesTheme

private class CoverColor(val argb: Long, @param:StringRes val nameRes: Int)

private val CoverColors = listOf(
    CoverColor(0xFF2962FF, R.string.color_blue),
    CoverColor(0xFFD32F2F, R.string.color_red),
    CoverColor(0xFFEF6C00, R.string.color_orange),
    CoverColor(0xFF2E7D32, R.string.color_green),
    CoverColor(0xFF00796B, R.string.color_teal),
    CoverColor(0xFF6A1B9A, R.string.color_purple),
    CoverColor(0xFFC2185B, R.string.color_pink),
    CoverColor(0xFF4E342E, R.string.color_brown),
    CoverColor(0xFF37474F, R.string.color_slate),
    CoverColor(0xFF212121, R.string.color_black),
)

@StringRes
private fun CoverStyle.labelRes(): Int = when (this) {
    CoverStyle.PLAIN -> R.string.cover_style_plain
    CoverStyle.LEATHER -> R.string.cover_style_leather
    CoverStyle.LINEN -> R.string.cover_style_linen
    CoverStyle.GRADIENT -> R.string.cover_style_gradient
}

/** Style chooser (live previews of each texture in the current color) plus a color swatch row. */
@Composable
fun CoverPicker(
    cover: NotebookCover,
    onCoverChange: (NotebookCover) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel(R.string.create_sheet_cover_section)
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CoverStyle.entries.forEach { style ->
                    PickerOption(
                        selected = style == cover.style,
                        label = stringResource(style.labelRes()),
                        onClick = { onCoverChange(cover.copy(style = style)) },
                    ) {
                        NotebookCoverArt(
                            cover = cover.copy(style = style),
                            modifier = Modifier.size(width = 48.dp, height = 64.dp),
                        )
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel(R.string.create_sheet_color_section)
            FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CoverColors.forEach { swatch ->
                    val color = Color(swatch.argb)
                    val selected = swatch.argb.toInt() == cover.colorArgb
                    val name = stringResource(swatch.nameRes)
                    Box(
                        modifier = Modifier
                            .size(48.dp) // minimum touch target
                            .clip(CircleShape)
                            .selectable(selected = selected, role = Role.RadioButton) {
                                onCoverChange(cover.copy(colorArgb = swatch.argb.toInt()))
                            }
                            .semantics { contentDescription = name },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SectionLabel(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Selectable tile (radio semantics) with a preview slot and a caption. Min touch target >= 48dp. */
@Composable
internal fun PickerOption(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    preview: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .width(72.dp)
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .border(
                border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = shape,
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        preview()
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun CoverPickerPreview() {
    GreatNotesTheme(dynamicColor = false) {
        CoverPicker(NotebookCover.DEFAULT, onCoverChange = {}, modifier = Modifier.padding(16.dp))
    }
}
