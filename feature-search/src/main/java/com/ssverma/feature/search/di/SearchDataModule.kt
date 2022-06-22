package com.ssverma.feature.search.di

import com.ssverma.api.service.tmdb.response.RemoteMultiSearchSuggestion
import com.ssverma.feature.search.data.local.DefaultSearchLocalDataSource
import com.ssverma.feature.search.data.local.SearchLocalDataSource
import com.ssverma.feature.search.data.mapper.SearchSuggestionMapper
import com.ssverma.feature.search.data.mapper.SearchSuggestionsMapper
import com.ssverma.feature.search.data.remote.DefaultSearchRemoteDataSource
import com.ssverma.feature.search.data.remote.SearchRemoteDataSource
import com.ssverma.feature.search.data.repository.DefaultSearchRepository
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.feature.search.domain.repository.SearchRepository
import com.ssverma.shared.data.mapper.ListMapper
import com.ssverma.shared.data.mapper.Mapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchDataModule {

    @Binds
    abstract fun provideSearchSuggestionMapper(
        searchSuggestionMapper: SearchSuggestionMapper
    ): Mapper<RemoteMultiSearchSuggestion, SearchSuggestion>

    @Binds
    abstract fun provideSearchSuggestionsMapper(
        searchSuggestionsMapper: SearchSuggestionsMapper
    ): ListMapper<RemoteMultiSearchSuggestion, SearchSuggestion>

    @Binds
    abstract fun provideSearchRemoteDataSource(
        defaultSearchRemoteDataSource: DefaultSearchRemoteDataSource
    ): SearchRemoteDataSource

    @Binds
    abstract fun provideSearchLocalDataSource(
        defaultSearchLocalDataSource: DefaultSearchLocalDataSource
    ): SearchLocalDataSource

    @Binds
    abstract fun provideSearchRepository(
        defaultSearchRepository: DefaultSearchRepository
    ): SearchRepository
}