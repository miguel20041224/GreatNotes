package com.greatnotes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.greatnotes.data.local.entity.PageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Abstract class (not interface) so that @Transaction methods can have bodies.
 * Index maintenance and the denormalized notebook pageCount are updated in the same
 * transaction as the insert/delete.
 */
@Dao
abstract class PageDao {
    @Query("SELECT * FROM pages WHERE notebookId = :notebookId ORDER BY pageIndex ASC")
    abstract fun observeByNotebook(notebookId: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE id = :id")
    abstract suspend fun getById(id: String): PageEntity?

    @Query("SELECT id FROM pages WHERE notebookId = :notebookId")
    abstract suspend fun getPageIds(notebookId: String): List<String>

    @Query("SELECT COUNT(*) FROM pages WHERE notebookId = :notebookId")
    abstract suspend fun countByNotebook(notebookId: String): Int

    @Query("SELECT COUNT(*) > 0 FROM notebooks WHERE id = :notebookId")
    abstract suspend fun notebookExists(notebookId: String): Boolean

    @Query("UPDATE pages SET updatedAt = :updatedAt WHERE id = :pageId")
    abstract suspend fun touch(pageId: String, updatedAt: Long): Int

    @Insert
    protected abstract suspend fun insert(entity: PageEntity)

    @Query("DELETE FROM pages WHERE id = :pageId")
    protected abstract suspend fun deleteById(pageId: String)

    @Query("UPDATE pages SET pageIndex = pageIndex + 1 WHERE notebookId = :notebookId AND pageIndex >= :fromIndex")
    protected abstract suspend fun shiftIndicesUp(notebookId: String, fromIndex: Int)

    @Query("UPDATE pages SET pageIndex = pageIndex - 1 WHERE notebookId = :notebookId AND pageIndex > :afterIndex")
    protected abstract suspend fun shiftIndicesDown(notebookId: String, afterIndex: Int)

    @Query(
        "UPDATE notebooks SET pageCount = (SELECT COUNT(*) FROM pages WHERE notebookId = :notebookId), " +
            "updatedAt = :now WHERE id = :notebookId",
    )
    protected abstract suspend fun syncNotebookStats(notebookId: String, now: Long)

    /**
     * Inserts [page] at the clamped index, shifting later pages.
     *
     * @return the stored entity with the effective pageIndex.
     * @throws NoSuchElementException if the notebook does not exist.
     */
    @Transaction
    open suspend fun insertAt(page: PageEntity): PageEntity {
        if (!notebookExists(page.notebookId)) {
            throw NoSuchElementException("Notebook ${page.notebookId} does not exist")
        }
        val count = countByNotebook(page.notebookId)
        val placed = page.copy(pageIndex = page.pageIndex.coerceIn(0, count))
        shiftIndicesUp(placed.notebookId, placed.pageIndex)
        insert(placed)
        syncNotebookStats(placed.notebookId, page.updatedAt)
        return placed
    }

    /** @return true if the page existed. */
    @Transaction
    open suspend fun deleteAndCompact(pageId: String, now: Long): Boolean {
        val page = getById(pageId) ?: return false
        deleteById(pageId)
        shiftIndicesDown(page.notebookId, page.pageIndex)
        syncNotebookStats(page.notebookId, now)
        return true
    }
}
