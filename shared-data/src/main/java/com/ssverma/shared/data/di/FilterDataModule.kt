package com.ssverma.shared.data.di

import com.ssverma.shared.data.repository.FilterRepositoryImpl
import com.ssverma.shared.domain.filter.FilterProvider
import com.ssverma.shared.domain.filter.MovieFilter
import com.ssverma.shared.domain.filter.TvFilter
import com.ssverma.shared.domain.repository.FilterRepository
import com.ssverma.shared.domain.usecase.filter.FilterType
import com.ssverma.shared.domain.usecase.filter.GetFiltersUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FilterDataModule {

    @Binds
    @Singleton
    abstract fun bindFilterRepository(
        repository: FilterRepositoryImpl
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
