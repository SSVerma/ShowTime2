package com.ssverma.shared.domain.usecase.discovery

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.domain.repository.DiscoveryRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetUniversalDiscoveryUseCase @Inject constructor(
    private val discoveryRepository: DiscoveryRepository,
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(
        filter: UniversalDiscoveryFilter,
        page: Int = 1
    ): Result<List<UniversalMediaItem>, Failure.CoreFailure> {
        val result = discoveryRepository.discoverUniversal(filter, page)

        if (result !is Result.Success) {
            return result
        }

        val watchedHistory = libraryRepository.getAllWatchHistory().first()
        val watchedIds = watchedHistory.map { it.mediaId }.toSet()

        val favorites = libraryRepository.getAllFavorites().first()
        val favoriteIds = favorites.map { it.mediaId }.toSet()

        val watchlist = libraryRepository.getAllWatchlist().first()
        val watchlistIds = watchlist.map { it.mediaId }.toSet()

        val items = result.data

        val filteredItems = if (filter.hideWatched) {
            items.filterNot { watchedIds.contains(it.id) }
        } else {
            items
        }

        val enrichedItems = filteredItems.map { item ->
            item.copy(
                isWatched = watchedIds.contains(item.id),
                isFavorite = favoriteIds.contains(item.id),
                isInWatchlist = watchlistIds.contains(item.id)
            )
        }

        return Result.Success(enrichedItems)
    }
}
