package com.ssverma.shared.domain.model.movie

import com.ssverma.shared.domain.model.*
import java.time.LocalDate

class Movie(
    val id: Int,
    val imdbId: String?,
    val title: String,
    val tagline: String?,
    val overview: String,
    val posterImageUrl: String,
    val backdropImageUrl: String,
    val budget: Long,
    val status: String,
    val videoAvailable: Boolean,
    val voteAvg: Float,
    val voteAvgPercentage: Float,
    val voteCount: Int,
    val releaseDate: LocalDate?,
    val displayReleaseDate: String?,
    val revenue: Long,
    val runtime: Int,
    val popularity: Float,
    val displayPopularity: String,
    val originalLanguage: String,
    val movieCollection: MovieCollection?,
    val casts: List<Cast>,
    val crews: List<Crew>,
    val keywords: List<Keyword>,
    val posters: List<ImageShot>,
    val backdrops: List<ImageShot>,
    val videos: List<Video>,
    val generes: List<Genre>,
    val reviews: List<Review>,
    val similarMovies: List<Movie>,
    val recommendations: List<Movie>,
    val watchProviders: Map<String, WatchProvider>
)

fun Movie.imageShots(): List<ImageShot> {
    return (backdrops + posters)
}