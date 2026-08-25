package com.ssverma.showtime.widget

import com.ssverma.feature.auth.data.local.TraktAuthStorage
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.domain.repository.TraktSyncRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun libraryRepository(): LibraryRepository
    fun traktSyncRepository(): TraktSyncRepository
    fun traktAuthStorage(): TraktAuthStorage
}
