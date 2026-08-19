package com.ssverma.shared.testing.fakes

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.SavedMediaItem
import com.ssverma.shared.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeLibraryRepository : LibraryRepository {

    private val favorites = MutableStateFlow<Map<Int, SavedMediaItem>>(emptyMap())
    private val watchlist = MutableStateFlow<Map<Int, SavedMediaItem>>(emptyMap())
    private val history = MutableStateFlow<List<SavedMediaItem>>(emptyList())

    override fun isFavoriteFlow(mediaId: Int): Flow<Boolean> {
        return favorites.map { it.containsKey(mediaId) }
    }

    override suspend fun isFavorite(mediaId: Int): Boolean {
        return favorites.value.containsKey(mediaId)
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
        val current = favorites.value.toMutableMap()
        val wasFav = current.containsKey(mediaId)
        if (wasFav) {
            current.remove(mediaId)
        } else {
            current[mediaId] = SavedMediaItem(
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
        favorites.value = current
        return !wasFav
    }

    override suspend fun deleteFavorite(mediaId: Int) {
        val current = favorites.value.toMutableMap()
        current.remove(mediaId)
        favorites.value = current
    }

    override fun isInWatchlistFlow(mediaId: Int): Flow<Boolean> {
        return watchlist.map { it.containsKey(mediaId) }
    }

    override suspend fun isInWatchlist(mediaId: Int): Boolean {
        return watchlist.value.containsKey(mediaId)
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
        val current = watchlist.value.toMutableMap()
        val wasInWatchlist = current.containsKey(mediaId)
        if (wasInWatchlist) {
            current.remove(mediaId)
        } else {
            current[mediaId] = SavedMediaItem(
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
        watchlist.value = current
        return !wasInWatchlist
    }

    override suspend fun deleteWatchlist(mediaId: Int) {
        val current = watchlist.value.toMutableMap()
        current.remove(mediaId)
        watchlist.value = current
    }

    override fun isWatchedFlow(mediaId: Int): Flow<Boolean> {
        return history.map { list -> list.any { it.mediaId == mediaId } }
    }

    override suspend fun isWatched(mediaId: Int): Boolean {
        return history.value.any { it.mediaId == mediaId }
    }

    override suspend fun toggleWatchHistory(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        voteAvg: Float
    ): Boolean {
        val wasWatched = history.value.any { it.mediaId == mediaId }
        if (wasWatched) {
            history.value = history.value.filterNot { it.mediaId == mediaId }
        } else {
            val item = SavedMediaItem(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                posterImageUrl = posterImageUrl,
                backdropImageUrl = "",
                voteAvg = voteAvg,
                releaseDate = "",
                addedAt = System.currentTimeMillis()
            )
            history.value = listOf(item) + history.value.filterNot { it.mediaId == mediaId }
        }
        return !wasWatched
    }

    override suspend fun logWatchHistory(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        voteAvg: Float
    ) {
        val item = SavedMediaItem(
            mediaId = mediaId,
            mediaType = mediaType,
            title = title,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = "",
            voteAvg = voteAvg,
            releaseDate = "",
            addedAt = System.currentTimeMillis()
        )
        history.value = listOf(item) + history.value.filterNot { it.mediaId == mediaId }
    }

    override suspend fun deleteWatchHistory(mediaId: Int) {
        history.value = history.value.filterNot { it.mediaId == mediaId }
    }

    override suspend fun clearWatchHistory() {
        history.value = emptyList()
    }

    override fun getAllFavorites(): Flow<List<SavedMediaItem>> {
        return favorites.map { it.values.toList() }
    }

    override fun getAllWatchlist(): Flow<List<SavedMediaItem>> {
        return watchlist.map { it.values.toList() }
    }

    override fun getAllWatchHistory(): Flow<List<SavedMediaItem>> {
        return history
    }

    override fun getFavoriteMovies(): Flow<List<SavedMediaItem>> {
        return favorites.map { map ->
            map.values.filter { it.mediaType == MediaType.Movie }
        }
    }

    override fun getFavoriteTvShows(): Flow<List<SavedMediaItem>> {
        return favorites.map { map ->
            map.values.filter { it.mediaType == MediaType.Tv }
        }
    }

    override fun getWatchlistMovies(): Flow<List<SavedMediaItem>> {
        return watchlist.map { map ->
            map.values.filter { it.mediaType == MediaType.Movie }
        }
    }

    override fun getWatchlistTvShows(): Flow<List<SavedMediaItem>> {
        return watchlist.map { map ->
            map.values.filter { it.mediaType == MediaType.Tv }
        }
    }

    override fun getWatchHistory(): Flow<List<SavedMediaItem>> {
        return history
    }

    private val customLists =
        MutableStateFlow<List<com.ssverma.shared.domain.model.library.CustomList>>(emptyList())

    override fun getCustomListsFlow(): Flow<List<com.ssverma.shared.domain.model.library.CustomList>> {
        return customLists
    }

    override fun getCustomListWithItemsFlow(listId: String): Flow<com.ssverma.shared.domain.model.library.CustomList?> {
        return customLists.map { lists -> lists.find { it.listId == listId } }
    }

    override suspend fun createCustomList(
        title: String,
        description: String?,
        coverImageUrl: String?
    ): String {
        val id = java.util.UUID.randomUUID().toString()
        val newList = com.ssverma.shared.domain.model.library.CustomList(
            listId = id,
            title = title,
            description = description,
            coverImageUrl = coverImageUrl,
            items = emptyList()
        )
        customLists.value = customLists.value + newList
        return id
    }

    override suspend fun updateCustomList(listId: String, title: String, description: String?) {
        customLists.value = customLists.value.map { list ->
            if (list.listId == listId) {
                list.copy(title = title, description = description)
            } else list
        }
    }

    override suspend fun deleteCustomList(listId: String) {
        customLists.value = customLists.value.filterNot { it.listId == listId }
    }

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
        val item = com.ssverma.shared.domain.model.library.CustomListItem(
            listId = listId,
            mediaId = mediaId,
            mediaType = mediaType,
            title = title,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = backdropImageUrl,
            voteAvg = voteAvg,
            userNotes = userNotes
        )
        customLists.value = customLists.value.map { list ->
            if (list.listId == listId) {
                list.copy(items = list.items.filterNot { it.mediaId == mediaId } + item)
            } else list
        }
    }

    override suspend fun removeMediaFromCustomList(listId: String, mediaId: Int) {
        customLists.value = customLists.value.map { list ->
            if (list.listId == listId) {
                list.copy(items = list.items.filterNot { it.mediaId == mediaId })
            } else list
        }
    }

    override fun getCustomListIdsForMediaFlow(mediaId: Int): Flow<List<String>> {
        return customLists.map { lists ->
            lists.filter { list -> list.items.any { it.mediaId == mediaId } }.map { it.listId }
        }
    }
}
