package com.ssverma.feature.movie.di

import com.ssverma.api.service.tmdb.response.RemoteGenre
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.api.service.tmdb.response.RemoteReview
import com.ssverma.feature.movie.data.remote.DefaultMovieRemoteDataSource
import com.ssverma.feature.movie.data.remote.MovieRemoteDataSource
import com.ssverma.feature.movie.data.repository.DefaultMovieRepository
import com.ssverma.feature.movie.domain.repository.MovieRepository
import com.ssverma.shared.data.mapper.*
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.Review
import com.ssverma.shared.domain.model.movie.Movie
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MovieDataModule {

    @Binds
    abstract fun provideMovieMapper(
        movieMapper: MovieMapper
    ): Mapper<RemoteMovie, Movie>

    @Binds
    abstract fun provideMoviesMapper(
        moviesMapper: MoviesMapper
    ): ListMapper<RemoteMovie, Movie>

    @Binds
    abstract fun provideGenresMapper(
        genresMapper: GenresMapper
    ): ListMapper<RemoteGenre, Genre>

    @Binds
    abstract fun provideReviewsMapper(
        reviewsMapper: ReviewsMapper
    ): ListMapper<RemoteReview, Review>

    @Binds
    abstract fun provideMovieRemoteDataSource(
        defaultMovieRemoteDataSource: DefaultMovieRemoteDataSource
    ): MovieRemoteDataSource

    @Binds
    abstract fun provideMovieRepository(
        defaultMovieRepository: DefaultMovieRepository
    ): MovieRepository
}