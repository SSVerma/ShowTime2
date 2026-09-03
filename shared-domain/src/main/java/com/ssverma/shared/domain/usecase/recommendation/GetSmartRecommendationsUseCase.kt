package com.ssverma.shared.domain.usecase.recommendation

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.domain.model.discovery.DiscoveryVibePreset
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter
import com.ssverma.shared.domain.model.stats.RecommendationShelf
import com.ssverma.shared.domain.repository.DiscoveryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetSmartRecommendationsUseCase @Inject constructor(
    private val discoveryRepository: DiscoveryRepository
) {
    suspend operator fun invoke(
        filterType: DiaryFilterType = DiaryFilterType.ALL,
        selectedProviderIds: Set<Int> = emptySet(),
        watchRegion: String = "US",
        page: Int = 1
    ): Result<List<RecommendationShelf>, Failure.CoreFailure> = coroutineScope {
        val shelves = mutableListOf<RecommendationShelf>()

        val targetMediaType = when (filterType) {
            DiaryFilterType.ALL,
            DiaryFilterType.REWATCHES_ONLY,
            DiaryFilterType.FIVE_STARS_ONLY,
            DiaryFilterType.MOVIES_ONLY -> MediaType.Movie

            DiaryFilterType.TV_ONLY -> MediaType.Tv
        }

        val topPicksDeferred = async {
            discoveryRepository.discoverUniversal(
                filter = UniversalDiscoveryFilter(
                    mediaType = targetMediaType,
                    vibePreset = DiscoveryVibePreset.MIND_BENDING,
                    selectedProviderIds = selectedProviderIds,
                    watchRegion = watchRegion,
                    minRating = 7.0f
                ),
                page = page
            )
        }

        val masterpiecesDeferred = async {
            discoveryRepository.discoverUniversal(
                filter = UniversalDiscoveryFilter(
                    mediaType = targetMediaType,
                    vibePreset = DiscoveryVibePreset.MASTERPIECES,
                    selectedProviderIds = selectedProviderIds,
                    watchRegion = watchRegion,
                    minRating = 8.0f
                ),
                page = page
            )
        }

        val funDeferred = async {
            discoveryRepository.discoverUniversal(
                filter = UniversalDiscoveryFilter(
                    mediaType = targetMediaType,
                    vibePreset = DiscoveryVibePreset.PURE_FUN,
                    selectedProviderIds = selectedProviderIds,
                    watchRegion = watchRegion
                ),
                page = page
            )
        }

        val epicDeferred = async {
            discoveryRepository.discoverUniversal(
                filter = UniversalDiscoveryFilter(
                    mediaType = targetMediaType,
                    vibePreset = DiscoveryVibePreset.EPIC_WORLDS,
                    selectedProviderIds = selectedProviderIds,
                    watchRegion = watchRegion
                ),
                page = page
            )
        }

        val tvSpotlightDeferred = async {
            if (filterType == DiaryFilterType.ALL) {
                discoveryRepository.discoverUniversal(
                    filter = UniversalDiscoveryFilter(
                        mediaType = MediaType.Tv,
                        vibePreset = DiscoveryVibePreset.COMFORT_BINGE,
                        selectedProviderIds = selectedProviderIds,
                        watchRegion = watchRegion
                    ),
                    page = page
                )
            } else null
        }

        val topPicksResult = topPicksDeferred.await()
        val masterpiecesResult = masterpiecesDeferred.await()
        val funResult = funDeferred.await()
        val epicResult = epicDeferred.await()
        val tvSpotlightResult = tvSpotlightDeferred.await()

        if (topPicksResult is Result.Success && topPicksResult.data.isNotEmpty()) {
            shelves.add(
                RecommendationShelf(
                    id = "top_picks",
                    title = "Top Picks For You",
                    subtitle = "Curated based on your taste & high ratings",
                    badge = "98% Match",
                    emoji = "✨",
                    items = topPicksResult.data
                )
            )
        }

        if (masterpiecesResult is Result.Success && masterpiecesResult.data.isNotEmpty()) {
            shelves.add(
                RecommendationShelf(
                    id = "masterpieces",
                    title = "Acclaimed Masterpieces",
                    subtitle = "Highest rated cinema with 8.0+ TMDB score",
                    badge = "★ 8.0+ Rating",
                    emoji = "🏆",
                    items = masterpiecesResult.data
                )
            )
        }

        if (funResult is Result.Success && funResult.data.isNotEmpty()) {
            shelves.add(
                RecommendationShelf(
                    id = "pure_fun",
                    title = "Crowd Pleasers",
                    subtitle = "Exciting, high-energy entertainment",
                    badge = "Trending",
                    emoji = "🍿",
                    items = funResult.data
                )
            )
        }

        if (epicResult is Result.Success && epicResult.data.isNotEmpty()) {
            shelves.add(
                RecommendationShelf(
                    id = "epic_worlds",
                    title = "Epic & Sci-Fi Worlds",
                    subtitle = "Immersive storytelling and spectacle",
                    badge = "Sci-Fi & Fantasy",
                    emoji = "🌌",
                    items = epicResult.data
                )
            )
        }

        if (tvSpotlightResult != null && tvSpotlightResult is Result.Success && tvSpotlightResult.data.isNotEmpty()) {
            shelves.add(
                RecommendationShelf(
                    id = "tv_spotlight",
                    title = "Binge-Worthy TV",
                    subtitle = "Hooking seasons and acclaimed shows",
                    badge = "TV Spotlight",
                    emoji = "📺",
                    items = tvSpotlightResult.data
                )
            )
        }

        Result.Success(shelves)
    }
}
