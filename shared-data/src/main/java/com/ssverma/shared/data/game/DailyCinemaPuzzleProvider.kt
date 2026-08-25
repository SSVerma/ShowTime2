package com.ssverma.shared.data.game

import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.api.service.tmdb.convertToTmdbBackdropUrl
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.shared.domain.model.game.DailyCinemaPuzzle
import com.ssverma.shared.domain.model.game.GameClue
import com.ssverma.shared.domain.model.game.GameClueType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyCinemaPuzzleProvider @Inject constructor(
    private val tmdbApiService: TmdbApiService
) {

    // Curated catalog of universally acclaimed, iconic movies on TMDB (reliable fallback & seed)
    private val curatedMovieIds = listOf(
        27205,  // Inception (2010)
        157336, // Interstellar (2014)
        155,    // The Dark Knight (2008)
        496243, // Parasite (2019)
        680,    // Pulp Fiction (1994)
        603,    // The Matrix (1999)
        129,    // Spirited Away (2001)
        872585, // Oppenheimer (2023)
        550,    // Fight Club (1999)
        244786, // Whiplash (2014)
        98,     // Gladiator (2000)
        693134, // Dune: Part Two (2024)
        278,    // The Shawshank Redemption (1994)
        324857, // Spider-Man: Into the Spider-Verse (2018)
        313369, // La La Land (2016)
        354912, // Coco (2017)
        335984, // Blade Runner 2049 (2017)
        497,    // The Green Mile (1999)
        120,    // The Lord of the Rings: The Fellowship of the Ring (2001)
        37799,  // The Social Network (2010)
        238,    // The Godfather (1972)
        429,    // The Good, the Bad and the Ugly (1966)
        13,     // Forrest Gump (1994)
        77338,  // The Intouchables (2011)
        568124  // Encanto (2021)
    )

    private var cachedMoviePool: List<Int>? = null

    private suspend fun getCandidateMoviePool(): List<Int> {
        val cached = cachedMoviePool
        if (!cached.isNullOrEmpty()) {
            return cached
        }

        return try {
            val response = tmdbApiService.getTrendingMovies(
                timeWindow = TmdbApiTiedConstants.AvailableTimeWindows.WEEK,
                page = 1
            )
            if (response is ApiResponse.Success) {
                val trendingIds = response.body.results?.filter { movie ->
                    (movie.voteCount
                        ?: 0) >= 150 && !movie.backdropPath.isNullOrBlank() && !movie.title.isNullOrBlank()
                }?.map { it.id }.orEmpty()

                val pool = if (trendingIds.isNotEmpty()) {
                    (trendingIds + curatedMovieIds).distinct()
                } else {
                    curatedMovieIds
                }
                cachedMoviePool = pool
                pool
            } else {
                curatedMovieIds
            }
        } catch (e: Exception) {
            curatedMovieIds
        }
    }

    suspend fun getPuzzleForEpochDay(
        epochDay: Long,
        attemptNumber: Int = 1
    ): DailyCinemaPuzzle = withContext(Dispatchers.IO) {
        val pool = getCandidateMoviePool()
        val offset = if (attemptNumber > 1) 13L else 0L
        val index = Math.floorMod(epochDay + offset, pool.size.toLong()).toInt()
        val targetMovieId = pool[index]
        val puzzleNumber = (epochDay - 20000).coerceAtLeast(1).toInt()

        val response = tmdbApiService.getMovieDetails(
            movieId = targetMovieId,
            queryMap = mapOf(
                TmdbApiTiedConstants.AppendToResponse to "${TmdbApiTiedConstants.AppendableResponseTypes.Images},${TmdbApiTiedConstants.AppendableResponseTypes.Credits}"
            )
        )

        if (response is ApiResponse.Success) {
            val movie = response.body
            val title = movie.title.orEmpty().ifBlank { "Inception" }
            val year = movie.releaseDate?.take(4) ?: "2010"
            val director = movie.credit?.crews?.firstOrNull {
                it.job.equals("Director", ignoreCase = true)
            }?.name ?: "Visionary Director"

            val leadCast = movie.credit?.casts?.take(3)?.mapNotNull { it.name }
                ?: listOf("Leading Cast")

            val tagline = movie.tagline?.ifBlank { null }
                ?: movie.overview?.take(90)?.plus("...")
                ?: "A cinematic masterpiece."

            val synopsis = movie.overview.orEmpty()
            val posterUrl = movie.posterPath.convertToTmdbPosterUrl()
            val backdropUrl = movie.backdropPath.convertToTmdbBackdropUrl()

            val secondBackdropPath = movie.imagePayload?.backdrops?.getOrNull(1)?.imagePath
            val sceneStillUrl =
                secondBackdropPath.convertToTmdbBackdropUrl().ifBlank { backdropUrl }

            val clues = listOf(
                GameClue(
                    clueNumber = 1,
                    type = GameClueType.BLURRED_SHOT,
                    label = "Visual Mystery",
                    content = "Can you identify this cinematic masterpiece from this frame?",
                    imageUrl = backdropUrl
                ),
                GameClue(
                    clueNumber = 2,
                    type = GameClueType.SCENE_STILL,
                    label = "Scene Still",
                    content = "A production still revealing location and visual aesthetics.",
                    imageUrl = sceneStillUrl
                ),
                GameClue(
                    clueNumber = 3,
                    type = GameClueType.RELEASE_YEAR,
                    label = "Release Era",
                    content = "This film premiered in theaters in $year.",
                    imageUrl = null
                ),
                GameClue(
                    clueNumber = 4,
                    type = GameClueType.CAST_DIRECTOR,
                    label = "Cast & Crew",
                    content = "Directed by $director • Starring ${leadCast.joinToString(", ")}.",
                    imageUrl = null
                ),
                GameClue(
                    clueNumber = 5,
                    type = GameClueType.PLOT_TAGLINE,
                    label = "Plot Tagline",
                    content = "\"$tagline\"",
                    imageUrl = null
                )
            )

            return@withContext DailyCinemaPuzzle(
                puzzleNumber = puzzleNumber,
                epochDay = epochDay,
                targetMovieId = targetMovieId,
                targetMovieTitle = title,
                releaseYear = year,
                director = director,
                leadCast = leadCast,
                tagline = tagline,
                synopsis = synopsis,
                posterImageUrl = posterUrl,
                backdropImageUrl = backdropUrl,
                clues = clues
            )
        } else {
            // Offline / fallback puzzle
            return@withContext createFallbackPuzzle(puzzleNumber, epochDay, targetMovieId)
        }
    }

    suspend fun getTodayPuzzle(attemptNumber: Int = 1): DailyCinemaPuzzle {
        val todayEpochDay = LocalDate.now(ZoneOffset.UTC).toEpochDay()
        return getPuzzleForEpochDay(todayEpochDay, attemptNumber)
    }

    private fun createFallbackPuzzle(
        puzzleNumber: Int,
        epochDay: Long,
        targetMovieId: Int
    ): DailyCinemaPuzzle {
        return DailyCinemaPuzzle(
            puzzleNumber = puzzleNumber,
            epochDay = epochDay,
            targetMovieId = targetMovieId,
            targetMovieTitle = "Inception",
            releaseYear = "2010",
            director = "Christopher Nolan",
            leadCast = listOf("Leonardo DiCaprio", "Joseph Gordon-Levitt", "Elliot Page"),
            tagline = "Your mind is the scene of the crime.",
            synopsis = "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.",
            posterImageUrl = "/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg".convertToTmdbPosterUrl(),
            backdropImageUrl = "/8ZTVqvKDQ8emSGUEMjsS4yHAwrp.jpg".convertToTmdbBackdropUrl(),
            clues = listOf(
                GameClue(
                    1,
                    GameClueType.BLURRED_SHOT,
                    "Visual Mystery",
                    "Can you identify this film?",
                    "/8ZTVqvKDQ8emSGUEMjsS4yHAwrp.jpg".convertToTmdbBackdropUrl()
                ),
                GameClue(
                    2,
                    GameClueType.SCENE_STILL,
                    "Scene Still",
                    "A production still revealing scene aesthetics.",
                    "/s3TBrRGB1iav7gFOCNx3H31MoES.jpg".convertToTmdbBackdropUrl()
                ),
                GameClue(
                    3,
                    GameClueType.RELEASE_YEAR,
                    "Release Era",
                    "This film premiered in theaters in 2010.",
                    null
                ),
                GameClue(
                    4,
                    GameClueType.CAST_DIRECTOR,
                    "Cast & Crew",
                    "Directed by Christopher Nolan • Starring Leonardo DiCaprio.",
                    null
                ),
                GameClue(
                    5,
                    GameClueType.PLOT_TAGLINE,
                    "Plot Tagline",
                    "\"Your mind is the scene of the crime.\"",
                    null
                )
            )
        )
    }
}
