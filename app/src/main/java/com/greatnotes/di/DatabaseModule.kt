package com.greatnotes.di

import android.content.Context
import androidx.room.Room
import com.greatnotes.data.local.dao.NotebookDao
import com.greatnotes.data.local.dao.PageDao
import com.greatnotes.data.local.database.ALL_MIGRATIONS
import com.greatnotes.data.local.database.GreatNotesDatabase
import com.greatnotes.data.local.file.StrokeFileDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GreatNotesDatabase =
        Room.databaseBuilder(context, GreatNotesDatabase::class.java, GreatNotesDatabase.DATABASE_NAME)
            .addMigrations(*ALL_MIGRATIONS)
            .build()

    @Provides
    fun provideNotebookDao(db: GreatNotesDatabase): NotebookDao = db.notebookDao()

    @Provides
    fun providePageDao(db: GreatNotesDatabase): PageDao = db.pageDao()

    @Provides
    @Singleton
    fun provideStrokeFileDataSource(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): StrokeFileDataSource = StrokeFileDataSource(File(context.filesDir, "strokes"), ioDispatcher)
}
