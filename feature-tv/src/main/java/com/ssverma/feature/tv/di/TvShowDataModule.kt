package com.ssverma.feature.tv.di

import com.ssverma.api.service.tmdb.response.RemoteTvEpisode
import com.ssverma.api.service.tmdb.response.RemoteTvSeason
import com.ssverma.api.service.tmdb.response.RemoteTvShow
import com.ssverma.feature.tv.data.mapper.TvEpisodeMapper
import com.ssverma.feature.tv.data.remote.DefaultTvShowRemoteDataSource
import com.ssverma.feature.tv.data.remote.TvShowRemoteDataSource
import com.ssverma.feature.tv.data.repository.DefaultTvShowRepository
import com.ssverma.feature.tv.domain.model.TvEpisode
import com.ssverma.feature.tv.domain.model.TvSeason
import com.ssverma.feature.tv.domain.model.TvShow
import com.ssverma.feature.tv.domain.repository.TvShowRepository
import com.ssverma.shared.data.mapper.ListMapper
import com.ssverma.shared.data.mapper.Mapper
import com.ssverma.showtime.data.mapper.TvSeasonMapper
import com.ssverma.feature.tv.data.mapper.TvShowMapper
import com.ssverma.feature.tv.data.mapper.TvShowsMapper
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