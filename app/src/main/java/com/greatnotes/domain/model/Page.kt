package com.greatnotes.domain.model

/**
 * A single page of a [Notebook]. Strokes are NOT part of this model: they are stored in a
 * binary file named [strokeFileName] and loaded through the stroke repository.
 *
 * Page size is expressed in PDF points (1 pt = 1/72 inch). [pdfSourceUri] and [pdfPageIndex]
 * are reserved for PDF-backed pages and are either both null or both set.
 */
data class Page(
    val id: String,
    val notebookId: String,
    val pageIndex: Int,
    val template: PageTemplate,
    val widthPt: Float,
    val heightPt: Float,
    val strokeFileName: String,
    val pdfSourceUri: String?,
    val pdfPageIndex: Int?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        const val STROKE_FILE_EXTENSION = "strokes"

        /** A4 portrait in points. */
        const val DEFAULT_WIDTH_PT = 595f
        const val DEFAULT_HEIGHT_PT = 842f

        /** Pass as the insertion index to append a page at the end of a notebook. */
        const val APPEND_INDEX = Int.MAX_VALUE

        /** Canonical stroke file name for a page id. */
        fun strokeFileNameFor(pageId: String): String = "$pageId.$STROKE_FILE_EXTENSION"
    }
}

enum class PageTemplate { BLANK, LINED, GRID, DOTTED }
