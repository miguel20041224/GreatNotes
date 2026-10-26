package com.greatnotes.data.mapper

import com.greatnotes.data.local.entity.PageEntity
import com.greatnotes.domain.model.Page

fun PageEntity.toDomain(): Page = Page(
    id = id,
    notebookId = notebookId,
    pageIndex = pageIndex,
    template = template,
    widthPt = widthPt,
    heightPt = heightPt,
    strokeFileName = strokeFileName,
    pdfSourceUri = pdfSourceUri,
    pdfPageIndex = pdfPageIndex,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Page.toEntity(): PageEntity = PageEntity(
    id = id,
    notebookId = notebookId,
    pageIndex = pageIndex,
    template = template,
    widthPt = widthPt,
    heightPt = heightPt,
    strokeFileName = strokeFileName,
    pdfSourceUri = pdfSourceUri,
    pdfPageIndex = pdfPageIndex,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
