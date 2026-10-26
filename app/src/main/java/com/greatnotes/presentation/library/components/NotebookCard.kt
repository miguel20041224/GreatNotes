package com.greatnotes.presentation.library.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.greatnotes.R
import com.greatnotes.domain.model.CoverStyle
import com.greatnotes.domain.model.Notebook
import com.greatnotes.domain.model.NotebookCover
import com.greatnotes.presentation.theme.GreatNotesTheme
import kotlin.random.Random

private val BookShape = RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp, topEnd = 10.dp, bottomEnd = 10.dp)

/** Book aspect ratio (width / height). */
internal const val BOOK_ASPECT_RATIO = 3f / 4f

@Composable
fun NotebookCard(
    notebook: Notebook,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // clickable merges descendants, so TalkBack reads "title, N pages, Open notebook, button".
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClickLabel = stringResource(R.string.library_open_notebook), onClick = onClick)
            .padding(4.dp),
    ) {
        NotebookCoverArt(
            cover = notebook.cover,
            modifier = Modifier.fillMaxWidth().aspectRatio(BOOK_ASPECT_RATIO),
        )
        Text(
            text = notebook.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = pluralStringResource(R.plurals.library_page_count, notebook.pageCount, notebook.pageCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Decorative cover rendering shared by the card and the cover picker. Not announced by TalkBack. */
@Composable
fun NotebookCoverArt(cover: NotebookCover, modifier: Modifier = Modifier) {
    val base = Color(cover.colorArgb)
    Box(modifier = modifier.clip(BookShape).then(
        if (cover.style == CoverStyle.GRADIENT) Modifier.background(
            Brush.linearGradient(
                colors = listOf(lerp(base, Color.White, 0.35f), base, lerp(base, Color.Black, 0.45f)),
                start = Offset.Zero,
                end = Offset.Infinite,
            ),
        ) else Modifier.background(base),
    )) {
        Canvas(Modifier.fillMaxSize()) {
            when (cover.style) {
                CoverStyle.LEATHER -> {
                    val rnd = Random(LEATHER_SEED)
                    val maxR = 1.6.dp.toPx()
                    repeat(260) {
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.12f),
                            radius = rnd.nextFloat() * maxR + 0.4.dp.toPx(),
                            center = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height),
                        )
                    }
                    repeat(140) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = rnd.nextFloat() * maxR * 0.7f + 0.3.dp.toPx(),
                            center = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height),
                        )
                    }
                }
                CoverStyle.LINEN -> {
                    val step = 3.dp.toPx()
                    val stroke = 0.8.dp.toPx()
                    var x = 0f
                    while (x < size.width) {
                        drawLine(Color.Black.copy(alpha = 0.10f), Offset(x, 0f), Offset(x, size.height), stroke)
                        x += step
                    }
                    var y = step / 2
                    while (y < size.height) {
                        drawLine(Color.White.copy(alpha = 0.10f), Offset(0f, y), Offset(size.width, y), stroke)
                        y += step
                    }
                }
                CoverStyle.PLAIN, CoverStyle.GRADIENT -> Unit
            }
            // Spine shadow + highlight on the left edge.
            val spine = 10.dp.toPx()
            drawRect(Color.Black.copy(alpha = 0.20f), size = androidx.compose.ui.geometry.Size(spine, size.height))
            drawLine(Color.White.copy(alpha = 0.25f), Offset(spine, 0f), Offset(spine, size.height), 1.dp.toPx())
        }
    }
}

private const val LEATHER_SEED = 42

private fun previewNotebook(id: String, title: String, style: CoverStyle, color: Long) = Notebook(
    id = id, title = title, cover = NotebookCover(style, color.toInt()), createdAt = 0, updatedAt = 0,
    pageCount = 12, isArchived = false, sortIndex = 0,
)

@Preview(showBackground = true)
@Composable
private fun NotebookCardPreview() {
    GreatNotesTheme(dynamicColor = false) {
        Column(Modifier.padding(16.dp)) {
            NotebookCard(previewNotebook("1", "Physics notes", CoverStyle.LEATHER, 0xFF4E342E), onClick = {})
        }
    }
}
