package com.ssverma.shared.ai.tools

import androidx.appfunctions.AppFunctionDeclaration
import com.ssverma.shared.ai.tools.model.CinephileMovieSearchResult
import com.ssverma.shared.ai.tools.model.CinephileMovieSummary
import com.ssverma.shared.ai.tools.model.CinephileVibeResult
import com.ssverma.shared.ai.tools.model.CinephileWatchlistResult
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.repository.DiscoveryRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device tools exposed to Gemini Nano 4 and the Android Intelligence System via AppFunctions.
 */
@Singleton
class CinephileMovieTools @Inject constructor(
    private val discoveryRepository: DiscoveryRepository,
    private val libraryRepository: LibraryRepository
) {

    /**
     * Searches for movies in ShowTime matching a query or thematic genre description.
     *
     * @param query The search keyword or film title.
     * @param primaryGenre Optional primary genre name like "Drama", "Sci-Fi", "Comedy".
     * @return List of matching movies with metadata.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun search_movies(
        query: String,
        primaryGenre: String = ""
    ): CinephileMovieSearchResult {
        val config = MovieDiscoverConfig.builder().build()
        val result = discoveryRepository.discoverMovies(discoverConfig = config)

        val summaries = when (result) {
            is Result.Success -> {
                result.data
                    .filter { movie ->
                        if (query.isNotBlank()) {
                            movie.title.contains(query, ignoreCase = true) ||
                                    movie.overview.contains(query, ignoreCase = true)
                        } else {
                            true
                        }
                    }
                    .take(5)
                    .map { movie ->
                        CinephileMovieSummary(
                            id = movie.id,
                            title = movie.title,
                            releaseYear = movie.releaseDate?.year?.toString() ?: "",
                            rating = movie.voteAvg,
                            overview = movie.overview,
                            posterPath = movie.posterImageUrl
                        )
                    }
            }

            is Result.Error -> emptyList()
        }

        return CinephileMovieSearchResult(
            query = query,
            movies = summaries
        )
    }

    /**
     * Adds or removes a movie from the user's personal ShowTime watchlist.
     *
     * @param mediaId The unique TMDB identifier of the movie.
     * @param title The title of the movie.
     * @return Confirmation status indicating whether the item is now in the watchlist.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun toggle_watchlist(
        mediaId: Int,
        title: String
    ): CinephileWatchlistResult {
        val isAdded = libraryRepository.toggleWatchlist(
            mediaId = mediaId,
            mediaType = MediaType.Movie,
            title = title,
            posterImageUrl = "",
            backdropImageUrl = "",
            voteAvg = 0.0f,
            releaseDate = ""
        )

        val message = if (isAdded) {
            "Added \"$title\" to your ShowTime watchlist."
        } else {
            "Removed \"$title\" from your ShowTime watchlist."
        }

        return CinephileWatchlistResult(
            mediaId = mediaId,
            isAdded = isAdded,
            message = message
        )
    }

    /**
     * Retrieves cinephile vibe and tone tags for a specific film.
     *
     * @param mediaId The unique TMDB identifier of the film.
     * @return Vibe and atmosphere tags describing the film's mood.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    fun get_cinephile_vibe_tags(
        mediaId: Int
    ): CinephileVibeResult {
        val fallbackVibes = listOf(
            "atmospheric",
            "thought-provoking",
            "cinematography-driven",
            "character-study"
        )
        return CinephileVibeResult(
            mediaId = mediaId,
            vibeTags = fallbackVibes
        )
    }
}
