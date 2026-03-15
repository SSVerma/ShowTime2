package com.ssverma.feature.filter.di

import com.ssverma.feature.filter.data.repository.DefaultFilterRepository
import com.ssverma.feature.filter.domain.FilterProvider
import com.ssverma.feature.filter.domain.MovieFilter
import com.ssverma.feature.filter.domain.TvFilter
import com.ssverma.feature.filter.domain.repository.FilterRepository
import com.ssverma.feature.filter.domain.usecase.FilterType
import com.ssverma.feature.filter.domain.usecase.GetFiltersUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FilterModule {

    @Binds
    @Singleton
    abstract fun bindFilterRepository(
        repository: DefaultFilterRepository
    ): FilterRepository

    companion object {
        @Provides
        @MovieFilter
        fun provideMovieFilterProvider(
            getFiltersUseCaseProvider: Provider<GetFiltersUseCase>
        ): FilterProvider {
            return getFiltersUseCaseProvider.get().apply {
                setFilterType(FilterType.Movie)
            }
        }

        @Provides
        @TvFilter
        fun provideTvFilterProvider(
            getFiltersUseCaseProvider: Provider<GetFiltersUseCase>
        ): FilterProvider {
            return getFiltersUseCaseProvider.get().apply {
                setFilterType(FilterType.Tv)
            }
        }
    }
}
