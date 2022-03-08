package com.ssverma.showtime.data.mapper

import com.ssverma.api.service.tmdb.TMDB_IMAGE_BASE_URL
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.utils.CoreUtils
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.FormatterUtils
import com.ssverma.showtime.utils.formatLocally
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MovieMapper @Inject constructor() : Mapper<RemoteMovie, Movie>() {
    override suspend fun map(input: RemoteMovie): Movie {
        return input.asMovie()
    }
}

class MoviesMapper @Inject constructor() : ListMapper<RemoteMovie, Movie>() {
    override suspend fun mapItem(input: RemoteMovie): Movie {
        return input.asMovie()
    }
}

suspend fun RemoteMovie.asMovie(): Movie {
    return Movie(
        id = id,
        imdbId = imdbId,
        title = title.orEmpty(),
        tagline = if (tagline.isNullOrEmpty()) null else tagline,
        overview = overview ?: "",
        posterImageUrl = CoreUtils.buildImageUrl(TMDB_IMAGE_BASE_URL, posterPath),
        backdropImageUrl = CoreUtils.buildImageUrl(TMDB_IMAGE_BASE_URL, backdropPath),
        budget = budget,
        status = status.orEmpty(),
        videoAvailable = videoAvailable,
        voteAvg = voteAvg,
        voteAvgPercentage = voteAvg * 10f,
        voteCount = voteCount,
        displayReleaseDate = DateUtils.parseIsoDate(releaseDate)?.formatLocally(),
        revenue = revenue,
        runtime = runtime,
        popularity = popularity,
        displayPopularity = FormatterUtils.toRangeSymbol(popularity),
        originalLanguage = originalLanguage ?: "",
        casts = credit?.casts?.asCasts() ?: emptyList(),
        crews = credit?.crews?.asCrews() ?: emptyList(),
        keywords = keywordPayload?.keywords?.asKeywords() ?: emptyList(),
        posters = imagePayload?.posters?.asImagesShots() ?: emptyList(),
        backdrops = imagePayload?.backdrops?.asImagesShots() ?: emptyList(),
        videos = videoPayload?.videos?.filterYoutubeVideos() ?: emptyList(),
        generes = genres?.asGenres() ?: emptyList(),
        movieCollection = collection?.asMovieCollection(),
        reviews = reviews?.results?.asReviews()?.asReversed() ?: emptyList(),
        similarMovies = similarMovies?.results?.asMovies() ?: emptyList(),
        recommendations = recommendations?.results?.asMovies() ?: emptyList(),
    )
}

suspend fun List<RemoteMovie>.asMovies(): List<Movie> {
    return withContext(Dispatchers.Default) {
        map { it.asMovie() }
    }
}