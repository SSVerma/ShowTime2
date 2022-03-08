package com.ssverma.showtime.di

import com.ssverma.api.service.tmdb.response.RemoteGenre
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.api.service.tmdb.response.RemoteReview
import com.ssverma.showtime.data.mapper.*
import com.ssverma.showtime.data.remote.DefaultMovieRemoteDataSource
import com.ssverma.showtime.data.remote.MovieRemoteDataSource
import com.ssverma.showtime.data.repository.DefaultMovieRepository
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.domain.model.Review
import com.ssverma.showtime.domain.repository.MovieRepository
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