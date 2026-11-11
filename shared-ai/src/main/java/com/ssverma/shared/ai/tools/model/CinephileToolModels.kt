package com.ssverma.shared.ai.tools.model

import androidx.appfunctions.AppFunctionSerializable

/**
 * Condensed summary of a movie returned from on-device Cinephile tool executions.
 *
 * @property id Unique TMDB identifier.
 * @property title English display title of the film.
 * @property releaseYear 4-digit release year (e.g. "1994").
 * @property rating Average rating between 0.0 and 10.0.
 * @property overview Brief synopsis of the plot.
 * @property posterPath Relative poster image URL path.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class CinephileMovieSummary(
    val id: Int,
    val title: String,
    val releaseYear: String,
    val rating: Float,
    val overview: String,
    val posterPath: String
)

/**
 * Result payload containing a list of matched movies.
 *
 * @property query The search term or filter used.
 * @property movies List of matched films.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class CinephileMovieSearchResult(
    val query: String,
    val movies: List<CinephileMovieSummary>
)

/**
 * Result payload representing the outcome of a watchlist mutation.
 *
 * @property mediaId TMDB media identifier.
 * @property isAdded True if the item is now in the user's watchlist; false if removed.
 * @property message Status explanation for display or agent confirmation.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class CinephileWatchlistResult(
    val mediaId: Int,
    val isAdded: Boolean,
    val message: String
)

/**
 * Result payload containing vibe and mood tags for a film.
 *
 * @property mediaId TMDB media identifier.
 * @property vibeTags Descriptive tone tags (e.g. "atmospheric", "mind-bending", "noir").
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class CinephileVibeResult(
    val mediaId: Int,
    val vibeTags: List<String>
)
