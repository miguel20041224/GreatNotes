package com.greatnotes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.greatnotes.data.local.entity.NotebookEntity
import com.greatnotes.data.local.entity.PageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Abstract class (not interface) so that @Transaction methods can have bodies.
 */
@Dao
abstract class NotebookDao {
    @Query("SELECT * FROM notebooks WHERE isArchived = :archived ORDER BY sortIndex ASC, updatedAt DESC")
    abstract fun observeByArchived(archived: Boolean): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks WHERE id = :id")
    abstract fun observeById(id: String): Flow<NotebookEntity?>

    @Query("SELECT * FROM notebooks WHERE id = :id")
    abstract suspend fun getById(id: String): NotebookEntity?

    @Query("SELECT COALESCE(MAX(sortIndex) + 1, 0) FROM notebooks")
    abstract suspend fun nextSortIndex(): Int

    @Insert
    abstract suspend fun insert(entity: NotebookEntity)

    @Insert
    protected abstract suspend fun insertPage(entity: PageEntity)

    @Query("UPDATE notebooks SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    abstract suspend fun updateTitle(id: String, title: String, updatedAt: Long): Int

    @Query("UPDATE notebooks SET isArchived = :archived, updatedAt = :updatedAt WHERE id = :id")
    abstract suspend fun updateArchived(id: String, archived: Boolean, updatedAt: Long): Int

    /** Pages are removed by the ON DELETE CASCADE foreign key. */
    @Query("DELETE FROM notebooks WHERE id = :id")
    abstract suspend fun deleteById(id: String): Int

    /**
     * Inserts [notebook] and its first [page] atomically: either both rows exist afterwards or
     * neither does. No index shifting is needed because the notebook is brand new.
     */
    @Transaction
    open suspend fun insertWithFirstPage(notebook: NotebookEntity, page: PageEntity) {
        insert(notebook)
        insertPage(page)
    }
}
