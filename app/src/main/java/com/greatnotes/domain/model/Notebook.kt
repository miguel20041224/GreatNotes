package com.greatnotes.domain.model

/**
 * A notebook groups an ordered list of [Page]s.
 *
 * Timestamps are epoch milliseconds. [pageCount] is denormalized and maintained by the
 * data layer whenever pages are added or removed.
 */
data class Notebook(
    val id: String,
    val title: String,
    val cover: NotebookCover,
    val createdAt: Long,
    val updatedAt: Long,
    val pageCount: Int,
    val isArchived: Boolean,
    val sortIndex: Int,
) {
    companion object {
        const val MAX_TITLE_LENGTH = 200
    }
}

data class NotebookCover(
    val style: CoverStyle,
    val colorArgb: Int,
) {
    companion object {
        /** Opaque blue (0xFF2962FF). */
        val DEFAULT = NotebookCover(style = CoverStyle.PLAIN, colorArgb = 0xFF2962FF.toInt())
    }
}

enum class CoverStyle { PLAIN, LEATHER, LINEN, GRADIENT }
