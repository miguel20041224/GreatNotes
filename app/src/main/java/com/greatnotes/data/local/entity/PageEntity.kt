package com.greatnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.greatnotes.domain.model.PageTemplate

/**
 * The composite index leads with notebookId, so it also serves as the foreign key index.
 * It is intentionally NOT unique: reindexing (shift +/- 1) would transiently violate a
 * unique constraint in SQLite.
 */
@Entity(
    tableName = "pages",
    foreignKeys = [
        ForeignKey(
            entity = NotebookEntity::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["notebookId", "pageIndex"])],
)
data class PageEntity(
    @PrimaryKey val id: String,
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
)
