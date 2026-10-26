package com.greatnotes.data.mapper

import com.greatnotes.data.local.entity.NotebookEntity
import com.greatnotes.domain.model.Notebook
import com.greatnotes.domain.model.NotebookCover

fun NotebookEntity.toDomain(): Notebook = Notebook(
    id = id,
    title = title,
    cover = NotebookCover(style = coverStyle, colorArgb = coverColor),
    createdAt = createdAt,
    updatedAt = updatedAt,
    pageCount = pageCount,
    isArchived = isArchived,
    sortIndex = sortIndex,
)

fun Notebook.toEntity(): NotebookEntity = NotebookEntity(
    id = id,
    title = title,
    coverStyle = cover.style,
    coverColor = cover.colorArgb,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pageCount = pageCount,
    isArchived = isArchived,
    sortIndex = sortIndex,
)
