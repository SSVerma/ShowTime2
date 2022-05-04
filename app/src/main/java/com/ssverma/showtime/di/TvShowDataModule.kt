package com.ssverma.showtime.di

import com.ssverma.api.service.tmdb.response.RemoteTvEpisode
import com.ssverma.api.service.tmdb.response.RemoteTvSeason
import com.ssverma.api.service.tmdb.response.RemoteTvShow
import com.ssverma.showtime.data.mapper.*
import com.ssverma.showtime.data.remote.DefaultTvShowRemoteDataSource
import com.ssverma.showtime.data.remote.TvShowRemoteDataSource
import com.ssverma.showtime.data.repository.DefaultTvShowRepository
import com.ssverma.showtime.domain.model.TvEpisode
import com.ssverma.showtime.domain.model.TvSeason
import com.ssverma.showtime.domain.model.TvShow
import com.ssverma.showtime.domain.repository.TvShowRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TvShowDataModule {

    @Binds
    abstract fun provideTvShowMapper(
        tvShowMapper: TvShowMapper
    ): Mapper<RemoteTvShow, TvShow>

    @Binds
    abstract fun provideTvShowsMapper(
        tvShowsMapper: TvShowsMapper
    ): ListMapper<RemoteTvShow, TvShow>

    @Binds
    abstract fun provideTvSeasonMapper(
        tvSeasonMapper: TvSeasonMapper
    ): Mapper<RemoteTvSeason, TvSeason>

    @Binds
    abstract fun provideTvEpisodeMapper(
        tvEpisodeMapper: TvEpisodeMapper
    ): Mapper<RemoteTvEpisode, TvEpisode>

    @Binds
    abstract fun provideTvShowRemoteDataSource(
        defaultTvShowRemoteDataSource: DefaultTvShowRemoteDataSource
    ): TvShowRemoteDataSource

    @Binds
    abstract fun provideTvShowRepository(
        defaultTvShowRepository: DefaultTvShowRepository
    ): TvShowRepository
}