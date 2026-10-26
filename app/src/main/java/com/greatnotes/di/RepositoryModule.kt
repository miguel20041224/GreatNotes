package com.greatnotes.di

import com.greatnotes.data.repository.NotebookRepositoryImpl
import com.greatnotes.data.repository.PageRepositoryImpl
import com.greatnotes.data.repository.StrokeRepositoryImpl
import com.greatnotes.domain.repository.NotebookRepository
import com.greatnotes.domain.repository.PageRepository
import com.greatnotes.domain.repository.StrokeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindNotebookRepository(impl: NotebookRepositoryImpl): NotebookRepository

    @Binds
    abstract fun bindPageRepository(impl: PageRepositoryImpl): PageRepository

    @Binds
    abstract fun bindStrokeRepository(impl: StrokeRepositoryImpl): StrokeRepository
}
