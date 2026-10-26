package com.greatnotes.di

import com.greatnotes.domain.repository.NotebookRepository
import com.greatnotes.domain.repository.PageRepository
import com.greatnotes.domain.repository.StrokeRepository
import com.greatnotes.domain.usecase.AddPageUseCase
import com.greatnotes.domain.usecase.ArchiveNotebookUseCase
import com.greatnotes.domain.usecase.CreateNotebookUseCase
import com.greatnotes.domain.usecase.CreateNotebookWithFirstPageUseCase
import com.greatnotes.domain.usecase.DeleteNotebookUseCase
import com.greatnotes.domain.usecase.DeletePageUseCase
import com.greatnotes.domain.usecase.GetPageUseCase
import com.greatnotes.domain.usecase.LoadStrokesUseCase
import com.greatnotes.domain.usecase.ObserveNotebookUseCase
import com.greatnotes.domain.usecase.ObserveNotebooksUseCase
import com.greatnotes.domain.usecase.ObservePagesUseCase
import com.greatnotes.domain.usecase.RenameNotebookUseCase
import com.greatnotes.domain.usecase.SaveStrokesUseCase
import com.greatnotes.domain.util.IdGenerator
import com.greatnotes.domain.util.SystemTimeProvider
import com.greatnotes.domain.util.TimeProvider
import com.greatnotes.domain.util.UuidIdGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Use cases live in the pure-Kotlin domain layer and therefore cannot carry @Inject;
 * they are constructed here instead.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideTimeProvider(): TimeProvider = SystemTimeProvider

    @Provides
    fun provideIdGenerator(): IdGenerator = UuidIdGenerator

    @Provides
    fun provideObserveNotebooks(repo: NotebookRepository) = ObserveNotebooksUseCase(repo)

    @Provides
    fun provideObserveNotebook(repo: NotebookRepository) = ObserveNotebookUseCase(repo)

    @Provides
    fun provideCreateNotebook(repo: NotebookRepository, ids: IdGenerator, time: TimeProvider) =
        CreateNotebookUseCase(repo, ids, time)

    @Provides
    fun provideCreateNotebookWithFirstPage(repo: NotebookRepository, ids: IdGenerator, time: TimeProvider) =
        CreateNotebookWithFirstPageUseCase(repo, ids, time)

    @Provides
    fun provideRenameNotebook(repo: NotebookRepository, time: TimeProvider) =
        RenameNotebookUseCase(repo, time)

    @Provides
    fun provideArchiveNotebook(repo: NotebookRepository, time: TimeProvider) =
        ArchiveNotebookUseCase(repo, time)

    @Provides
    fun provideDeleteNotebook(repo: NotebookRepository) = DeleteNotebookUseCase(repo)

    @Provides
    fun provideObservePages(repo: PageRepository) = ObservePagesUseCase(repo)

    @Provides
    fun provideGetPage(repo: PageRepository) = GetPageUseCase(repo)

    @Provides
    fun provideAddPage(repo: PageRepository, ids: IdGenerator, time: TimeProvider) =
        AddPageUseCase(repo, ids, time)

    @Provides
    fun provideDeletePage(repo: PageRepository, time: TimeProvider) = DeletePageUseCase(repo, time)

    @Provides
    fun provideLoadStrokes(repo: StrokeRepository) = LoadStrokesUseCase(repo)

    @Provides
    fun provideSaveStrokes(repo: StrokeRepository) = SaveStrokesUseCase(repo)
}
