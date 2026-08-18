package com.ssverma.shared.testing.fakes

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeLibraryRepository : LibraryRepository {

    private val favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    private val watchlistIds = MutableStateFlow<Set<Int>>(emptySet())
    private val historyIds = MutableStateFlow<List<Int>>(emptyList())

    override fun isFavoriteFlow(mediaId: Int): Flow<Boolean> {
        return favoriteIds.map { it.contains(mediaId) }
    }

    override suspend fun isFavorite(mediaId: Int): Boolean {
        return favoriteIds.value.contains(mediaId)
    }

    override suspend fun toggleFavorite(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String,
        voteAvg: Float,
        releaseDate: String
    ): Boolean {
        val current = favoriteIds.value.toMutableSet()
        val wasFav = current.contains(mediaId)
        if (wasFav) {
            current.remove(mediaId)
        } else {
            current.add(mediaId)
        }
        favoriteIds.value = current
        return !wasFav
    }

    override fun isInWatchlistFlow(mediaId: Int): Flow<Boolean> {
        return watchlistIds.map { it.contains(mediaId) }
    }

    override suspend fun isInWatchlist(mediaId: Int): Boolean {
        return watchlistIds.value.contains(mediaId)
    }

    override suspend fun toggleWatchlist(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String,
        voteAvg: Float,
        releaseDate: String
    ): Boolean {
        val current = watchlistIds.value.toMutableSet()
        val wasInWatchlist = current.contains(mediaId)
        if (wasInWatchlist) {
            current.remove(mediaId)
        } else {
            current.add(mediaId)
        }
        watchlistIds.value = current
        return !wasInWatchlist
    }

    override suspend fun logWatchHistory(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        voteAvg: Float
    ) {
        historyIds.value = listOf(mediaId) + historyIds.value.filterNot { it == mediaId }
    }
}
