package com.ssverma.shared.domain.fakes

import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiarySummaryStats
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.model.library.SavedMediaItem
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.stats.CinephileMilestoneDefinition
import com.ssverma.shared.domain.model.stats.MilestoneActionType
import com.ssverma.shared.domain.model.stats.MilestoneMetricType
import com.ssverma.shared.domain.model.stats.MilestoneTier
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.repository.CinephileMilestoneRepository
import com.ssverma.shared.domain.repository.DiaryRepository
import com.ssverma.shared.domain.repository.DiscoveryRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeDiaryRepository : DiaryRepository {
    private val entries = MutableStateFlow<List<DiaryEntry>>(emptyList())

    override fun getAllDiaryEntries(): Flow<List<DiaryEntry>> = entries.asStateFlow()

    override fun getDiaryEntriesForMedia(
        mediaId: Int,
        mediaType: MediaType
    ): Flow<List<DiaryEntry>> {
        return entries.map { list -> list.filter { it.mediaId == mediaId && it.mediaType == mediaType } }
    }

    override suspend fun getDiaryEntryById(id: Long): DiaryEntry? {
        return entries.value.find { it.id == id }
    }

    override suspend fun saveDiaryEntry(entry: DiaryEntry): Long {
        val nextId =
            if (entry.id == 0L) (entries.value.maxOfOrNull { it.id } ?: 0L) + 1L else entry.id
        val finalEntry = entry.copy(id = nextId)
        entries.update { current ->
            val index =
                current.indexOfFirst { it.id == finalEntry.id || (it.mediaId == finalEntry.mediaId && it.mediaType == finalEntry.mediaType) }
            if (index != -1) {
                current.toMutableList().apply { set(index, finalEntry) }
            } else {
                current + finalEntry
            }
        }
        return nextId
    }

    override suspend fun deleteDiaryEntry(id: Long) {
        entries.update { current -> current.filterNot { it.id == id } }
    }

    override fun getDiarySummaryStats(): Flow<DiarySummaryStats> {
        return entries.map { list ->
            DiarySummaryStats(
                totalLogged = list.size,
                totalMovies = list.count { it.mediaType == MediaType.Movie },
                totalTvShows = list.count { it.mediaType == MediaType.Tv },
                averageUserRating = if (list.isNotEmpty()) list.map { it.userRating }.average()
                    .toFloat() else 0f,
                rewatchCount = list.count { it.isRewatch },
                fiveStarCount = list.count { it.userRating >= 5.0f }
            )
        }
    }
}

class FakeLibraryRepository : LibraryRepository {
    private val favorites = MutableStateFlow<List<SavedMediaItem>>(emptyList())
    private val watchlist = MutableStateFlow<List<SavedMediaItem>>(emptyList())
    private val history = MutableStateFlow<List<SavedMediaItem>>(emptyList())

    override fun isFavoriteFlow(mediaId: Int): Flow<Boolean> =
        favorites.map { it.any { item -> item.mediaId == mediaId } }

    override suspend fun isFavorite(mediaId: Int): Boolean =
        favorites.value.any { it.mediaId == mediaId }

    override fun isMediaActionActiveFlow(mediaId: Int): Flow<Boolean> = isFavoriteFlow(mediaId)
    override suspend fun isMediaActionActive(mediaId: Int): Boolean = isFavorite(mediaId)

    override suspend fun toggleFavorite(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String,
        voteAvg: Float,
        releaseDate: String
    ): Boolean {
        val exists = favorites.value.any { it.mediaId == mediaId }
        if (exists) {
            favorites.update { list -> list.filterNot { it.mediaId == mediaId } }
            return false
        } else {
            favorites.update { list ->
                list + SavedMediaItem(
                    mediaId = mediaId,
                    mediaType = mediaType,
                    title = title,
                    posterImageUrl = posterImageUrl,
                    backdropImageUrl = backdropImageUrl,
                    voteAvg = voteAvg,
                    releaseDate = releaseDate,
                    addedAt = System.currentTimeMillis()
                )
            }
            return true
        }
    }

    override suspend fun deleteFavorite(mediaId: Int) {
        favorites.update { list -> list.filterNot { it.mediaId == mediaId } }
    }

    override fun isInWatchlistFlow(mediaId: Int): Flow<Boolean> =
        watchlist.map { it.any { item -> item.mediaId == mediaId } }

    override suspend fun isInWatchlist(mediaId: Int): Boolean =
        watchlist.value.any { it.mediaId == mediaId }

    override suspend fun toggleWatchlist(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String,
        voteAvg: Float,
        releaseDate: String
    ): Boolean {
        val exists = watchlist.value.any { it.mediaId == mediaId }
        if (exists) {
            watchlist.update { list -> list.filterNot { it.mediaId == mediaId } }
            return false
        } else {
            watchlist.update { list ->
                list + SavedMediaItem(
                    mediaId = mediaId,
                    mediaType = mediaType,
                    title = title,
                    posterImageUrl = posterImageUrl,
                    backdropImageUrl = backdropImageUrl,
                    voteAvg = voteAvg,
                    releaseDate = releaseDate,
                    addedAt = System.currentTimeMillis()
                )
            }
            return true
        }
    }

    override suspend fun deleteWatchlist(mediaId: Int) {
        watchlist.update { list -> list.filterNot { it.mediaId == mediaId } }
    }

    override fun isWatchedFlow(mediaId: Int): Flow<Boolean> =
        history.map { it.any { item -> item.mediaId == mediaId } }

    override suspend fun isWatched(mediaId: Int): Boolean =
        history.value.any { it.mediaId == mediaId }

    override suspend fun toggleWatchHistory(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        voteAvg: Float
    ): Boolean {
        val exists = history.value.any { it.mediaId == mediaId }
        if (exists) {
            history.update { list -> list.filterNot { it.mediaId == mediaId } }
            return false
        } else {
            history.update { list ->
                list + SavedMediaItem(
                    mediaId = mediaId,
                    mediaType = mediaType,
                    title = title,
                    posterImageUrl = posterImageUrl,
                    backdropImageUrl = "",
                    voteAvg = voteAvg,
                    releaseDate = "",
                    addedAt = System.currentTimeMillis()
                )
            }
            return true
        }
    }

    override suspend fun logWatchHistory(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        voteAvg: Float
    ) {
        history.update { list ->
            list.filterNot { it.mediaId == mediaId } + SavedMediaItem(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                posterImageUrl = posterImageUrl,
                backdropImageUrl = "",
                voteAvg = voteAvg,
                releaseDate = "",
                addedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun deleteWatchHistory(mediaId: Int) {
        history.update { list -> list.filterNot { it.mediaId == mediaId } }
    }

    override suspend fun clearWatchHistory() {
        history.value = emptyList()
    }

    override suspend fun clearFavorites() {
        favorites.value = emptyList()
    }

    override suspend fun clearWatchlist() {
        watchlist.value = emptyList()
    }

    override suspend fun clearAllLibrary() {
        favorites.value = emptyList()
        watchlist.value = emptyList()
        history.value = emptyList()
    }

    override fun getAllFavorites(): Flow<List<SavedMediaItem>> = favorites.asStateFlow()
    override fun getAllWatchlist(): Flow<List<SavedMediaItem>> = watchlist.asStateFlow()
    override suspend fun getWatchlistSnapshot(): List<SavedMediaItem> = watchlist.value
    override fun getAllWatchHistory(): Flow<List<SavedMediaItem>> = history.asStateFlow()
    override fun getFavoriteMovies(): Flow<List<SavedMediaItem>> =
        favorites.map { it.filter { item -> item.mediaType == MediaType.Movie } }

    override fun getFavoriteTvShows(): Flow<List<SavedMediaItem>> =
        favorites.map { it.filter { item -> item.mediaType == MediaType.Tv } }

    override fun getWatchlistMovies(): Flow<List<SavedMediaItem>> =
        watchlist.map { it.filter { item -> item.mediaType == MediaType.Movie } }

    override fun getWatchlistTvShows(): Flow<List<SavedMediaItem>> =
        watchlist.map { it.filter { item -> item.mediaType == MediaType.Tv } }

    override fun getWatchHistory(): Flow<List<SavedMediaItem>> = history.asStateFlow()

    override fun getCustomListsFlow(): Flow<List<CustomList>> = MutableStateFlow(emptyList())
    override fun getCustomListWithItemsFlow(listId: String): Flow<CustomList?> =
        MutableStateFlow(null)

    override suspend fun createCustomList(
        title: String,
        description: String?,
        coverImageUrl: String?
    ): String = "1"

    override suspend fun updateCustomList(listId: String, title: String, description: String?) {}
    override suspend fun deleteCustomList(listId: String) {}
    override suspend fun addMediaToCustomList(
        listId: String,
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String,
        voteAvg: Float,
        userNotes: String?
    ) {
    }

    override suspend fun removeMediaFromCustomList(listId: String, mediaId: Int) {}
    override fun getCustomListIdsForMediaFlow(mediaId: Int): Flow<List<String>> =
        MutableStateFlow(emptyList())

    override suspend fun setCustomListPublicStatus(
        listId: String,
        isPublic: Boolean,
        fallbackList: CommunityCuratedList?
    ) {
    }

    override suspend fun cloneCommunityListToLocal(communityList: CommunityCuratedList): String =
        "1"
}

class FakeDiscoveryRepository : DiscoveryRepository {
    private val sampleItems = listOf(
        UniversalMediaItem(
            id = 1,
            mediaType = MediaType.Movie,
            title = "Dune: Part Two",
            overview = "Paul Atreides unites with Chani and the Fremen.",
            posterImageUrl = "/dune2.jpg",
            backdropImageUrl = "/dune2_backdrop.jpg",
            voteAvg = 8.5f,
            voteCount = 4500,
            releaseDate = "2024-03-01"
        ),
        UniversalMediaItem(
            id = 2,
            mediaType = MediaType.Tv,
            title = "Severance",
            overview = "Mark leads a team of office workers whose memories have been surgically divided.",
            posterImageUrl = "/severance.jpg",
            backdropImageUrl = "/severance_backdrop.jpg",
            voteAvg = 8.7f,
            voteCount = 2100,
            releaseDate = "2022-02-18"
        )
    )

    override suspend fun discoverMovies(discoverConfig: MovieDiscoverConfig): Result<List<Movie>, Failure.CoreFailure> =
        Result.Success(emptyList())

    override suspend fun discoverTvShows(discoverConfig: TvDiscoverConfig): Result<List<TvShow>, Failure.CoreFailure> =
        Result.Success(emptyList())

    override suspend fun discoverUniversal(
        filter: UniversalDiscoveryFilter,
        page: Int
    ): Result<List<UniversalMediaItem>, Failure.CoreFailure> {
        val filtered = sampleItems.filter { item ->
            if (filter.mediaType == MediaType.Movie) item.mediaType == MediaType.Movie
            else item.mediaType == MediaType.Tv
        }
        return Result.Success(filtered.ifEmpty { sampleItems })
    }

    override suspend fun fetchMovieGenre(): Result<List<Genre>, Failure.CoreFailure> =
        Result.Success(emptyList())

    override suspend fun fetchTvShowGenre(): Result<List<Genre>, Failure.CoreFailure> =
        Result.Success(emptyList())

    override suspend fun fetchWatchProviders(isMovie: Boolean): Result<List<ProviderInfo>, Failure.CoreFailure> =
        Result.Success(emptyList())
}

class FakeCinephileMilestoneRepository(
    initialDefinitions: List<CinephileMilestoneDefinition> = defaultMilestoneDefinitions
) : CinephileMilestoneRepository {

    private val definitionsFlow = MutableStateFlow(initialDefinitions)

    override val milestoneDefinitionsFlow: Flow<List<CinephileMilestoneDefinition>> =
        definitionsFlow.asStateFlow()

    override suspend fun getMilestoneDefinitions(forceRefresh: Boolean): List<CinephileMilestoneDefinition> =
        definitionsFlow.value

    fun setDefinitions(definitions: List<CinephileMilestoneDefinition>) {
        definitionsFlow.value = definitions
    }

    companion object {
        val defaultMilestoneDefinitions = listOf(
            CinephileMilestoneDefinition(
                id = "first_reel",
                title = "First Reel",
                iconEmoji = "🎬",
                description = "Log your very first film or series in Cinema Diary",
                category = "Volume",
                tier = MilestoneTier.BRONZE,
                maxProgress = 1,
                metricType = MilestoneMetricType.TOTAL_LOGS,
                actionType = MilestoneActionType.DIARY,
                actionLabel = "Log in Cinema Diary"
            ),
            CinephileMilestoneDefinition(
                id = "silver_explorer",
                title = "Silver Explorer",
                iconEmoji = "🧭",
                description = "Log 25 films or series entries",
                category = "Volume",
                tier = MilestoneTier.SILVER,
                maxProgress = 25,
                metricType = MilestoneMetricType.TOTAL_LOGS,
                actionType = MilestoneActionType.DIARY,
                actionLabel = "Log in Cinema Diary"
            ),
            CinephileMilestoneDefinition(
                id = "century_club",
                title = "Century Club",
                iconEmoji = "🏆",
                description = "Log 100 films or series in your diary",
                category = "Volume",
                tier = MilestoneTier.GOLD,
                maxProgress = 100,
                metricType = MilestoneMetricType.TOTAL_LOGS,
                actionType = MilestoneActionType.DIARY,
                actionLabel = "Log in Cinema Diary"
            ),
            CinephileMilestoneDefinition(
                id = "five_star_connoisseur",
                title = "Five-Star Connoisseur",
                iconEmoji = "⭐",
                description = "Award 10 perfect 5-star ratings",
                category = "Critical Taste",
                tier = MilestoneTier.GOLD,
                maxProgress = 10,
                metricType = MilestoneMetricType.FIVE_STAR_LOGS,
                actionType = MilestoneActionType.DIARY,
                actionLabel = "Log in Cinema Diary"
            ),
            CinephileMilestoneDefinition(
                id = "nostalgia_junkie",
                title = "Nostalgia Junkie",
                iconEmoji = "🔁",
                description = "Log 5 rewatches of comfort classics",
                category = "Dedication",
                tier = MilestoneTier.BRONZE,
                maxProgress = 5,
                metricType = MilestoneMetricType.REWATCHES,
                actionType = MilestoneActionType.DIARY,
                actionLabel = "Log a Rewatch"
            ),
            CinephileMilestoneDefinition(
                id = "marathon_master",
                title = "Marathon Master",
                iconEmoji = "⏱️",
                description = "Accumulate 100 hours of viewing time",
                category = "Endurance",
                tier = MilestoneTier.GOLD,
                maxProgress = 100,
                metricType = MilestoneMetricType.TOTAL_HOURS,
                actionType = MilestoneActionType.DIARY,
                actionLabel = "Log in Cinema Diary"
            ),
            CinephileMilestoneDefinition(
                id = "binge_overlord",
                title = "Binge Overlord",
                iconEmoji = "📺",
                description = "Log 25 television series seasons or entries",
                category = "Television",
                tier = MilestoneTier.SILVER,
                maxProgress = 25,
                metricType = MilestoneMetricType.TV_SHOWS,
                actionType = MilestoneActionType.DISCOVERY,
                actionLabel = "Discover TV Shows"
            ),
            CinephileMilestoneDefinition(
                id = "decade_hopper",
                title = "Decade Hopper",
                iconEmoji = "⏳",
                description = "Log films released across 5 different decades",
                category = "Diversity",
                tier = MilestoneTier.PLATINUM,
                maxProgress = 5,
                metricType = MilestoneMetricType.DECADE_COUNT,
                actionType = MilestoneActionType.DISCOVERY,
                actionLabel = "Explore Timeless Cinema"
            ),
            CinephileMilestoneDefinition(
                id = "curator_pro",
                title = "Curator Pro",
                iconEmoji = "💎",
                description = "Curate 3 personal cinema lists",
                category = "Curation",
                tier = MilestoneTier.DIAMOND,
                maxProgress = 3,
                metricType = MilestoneMetricType.CURATION_COUNT,
                actionType = MilestoneActionType.TASTE_PROFILE,
                actionLabel = "View Taste Profile"
            )
        )
    }
}

