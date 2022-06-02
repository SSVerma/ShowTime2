package com.ssverma.feature.movie.di

import com.ssverma.api.service.tmdb.response.RemoteGenre
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.api.service.tmdb.response.RemoteReview
import com.ssverma.core.domain.model.Genre
import com.ssverma.core.domain.model.Review
import com.ssverma.feature.movie.data.mapper.MovieMapper
import com.ssverma.feature.movie.data.mapper.MoviesMapper
import com.ssverma.feature.movie.domain.model.Movie
import com.ssverma.shared.data.mapper.GenresMapper
import com.ssverma.shared.data.mapper.ListMapper
import com.ssverma.shared.data.mapper.Mapper
import com.ssverma.shared.data.mapper.ReviewsMapper
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
        defaultMovieRemoteDataSource: com.ssverma.feature.movie.data.remote.DefaultMovieRemoteDataSource
    ): com.ssverma.feature.movie.data.remote.MovieRemoteDataSource

    @Binds
    abstract fun provideMovieRepository(
        defaultMovieRepository: com.ssverma.feature.movie.data.repository.DefaultMovieRepository
    ): com.ssverma.feature.movie.domain.repository.MovieRepository
}