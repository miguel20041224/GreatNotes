package com.greatnotes.data.mapper

import com.greatnotes.data.local.entity.NotebookEntity
import com.greatnotes.data.local.entity.PageEntity
import com.greatnotes.domain.model.CoverStyle
import com.greatnotes.domain.model.Notebook
import com.greatnotes.domain.model.NotebookCover
import com.greatnotes.domain.model.Page
import com.greatnotes.domain.model.PageTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MappersTest {

    private val notebook = Notebook(
        id = "nb-1",
        title = "Physics",
        cover = NotebookCover(CoverStyle.LEATHER, 0xFF336699.toInt()),
        createdAt = 10L,
        updatedAt = 20L,
        pageCount = 3,
        isArchived = true,
        sortIndex = 7,
    )

    private val page = Page(
        id = "pg-1",
        notebookId = "nb-1",
        pageIndex = 2,
        template = PageTemplate.DOTTED,
        widthPt = 595f,
        heightPt = 842f,
        strokeFileName = Page.strokeFileNameFor("pg-1"),
        pdfSourceUri = "content://docs/1",
        pdfPageIndex = 4,
        createdAt = 11L,
        updatedAt = 22L,
    )

    @Test
    fun notebook_roundTrip() {
        assertEquals(notebook, notebook.toEntity().toDomain())
    }

    @Test
    fun notebook_toEntity_flattensCover() {
        val entity = notebook.toEntity()
        assertEquals(CoverStyle.LEATHER, entity.coverStyle)
        assertEquals(0xFF336699.toInt(), entity.coverColor)
    }

    @Test
    fun notebookEntity_toDomain_buildsCover() {
        val entity = NotebookEntity("nb-2", "T", CoverStyle.GRADIENT, 5, 1L, 2L, 0, false, 0)
        assertEquals(NotebookCover(CoverStyle.GRADIENT, 5), entity.toDomain().cover)
    }

    @Test
    fun page_roundTrip() {
        assertEquals(page, page.toEntity().toDomain())
    }

    @Test
    fun page_roundTrip_withNullPdfFields() {
        val plain = page.copy(pdfSourceUri = null, pdfPageIndex = null)
        val entity: PageEntity = plain.toEntity()
        assertNull(entity.pdfSourceUri)
        assertNull(entity.pdfPageIndex)
        assertEquals(plain, entity.toDomain())
    }
}
