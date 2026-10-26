package com.greatnotes.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.greatnotes.data.local.dao.NotebookDao
import com.greatnotes.data.local.dao.PageDao
import com.greatnotes.data.local.entity.NotebookEntity
import com.greatnotes.data.local.entity.PageEntity

@Database(
    entities = [NotebookEntity::class, PageEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(EnumConverters::class)
abstract class GreatNotesDatabase : RoomDatabase() {
    abstract fun notebookDao(): NotebookDao
    abstract fun pageDao(): PageDao

    companion object {
        const val DATABASE_NAME = "greatnotes.db"
    }
}
