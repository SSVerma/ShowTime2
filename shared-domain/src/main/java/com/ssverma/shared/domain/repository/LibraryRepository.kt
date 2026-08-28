package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.model.library.SavedMediaItem
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun isFavoriteFlow(mediaId: Int): Flow<Boolean>
    suspend fun isFavorite(mediaId: Int): Boolean
    fun isMediaActionActiveFlow(mediaId: Int): Flow<Boolean>
    suspend fun isMediaActionActive(mediaId: Int): Boolean
    suspend fun toggleFavorite(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String,
        voteAvg: Float,
        releaseDate: String
    ): Boolean

    suspend fun deleteFavorite(mediaId: Int)

    fun isInWatchlistFlow(mediaId: Int): Flow<Boolean>
    suspend fun isInWatchlist(mediaId: Int): Boolean
    suspend fun toggleWatchlist(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String,
        voteAvg: Float,
        releaseDate: String
    ): Boolean

    suspend fun deleteWatchlist(mediaId: Int)

    fun isWatchedFlow(mediaId: Int): Flow<Boolean>
    suspend fun isWatched(mediaId: Int): Boolean
    suspend fun toggleWatchHistory(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        voteAvg: Float
    ): Boolean

    suspend fun logWatchHistory(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        voteAvg: Float
    )

    suspend fun deleteWatchHistory(mediaId: Int)
    suspend fun clearWatchHistory()
    suspend fun clearFavorites()
    suspend fun clearWatchlist()
    suspend fun clearAllLibrary()

    fun getAllFavorites(): Flow<List<SavedMediaItem>>
    fun getAllWatchlist(): Flow<List<SavedMediaItem>>
    suspend fun getWatchlistSnapshot(): List<SavedMediaItem>
    fun getAllWatchHistory(): Flow<List<SavedMediaItem>>

    fun getFavoriteMovies(): Flow<List<SavedMediaItem>>
    fun getFavoriteTvShows(): Flow<List<SavedMediaItem>>
    fun getWatchlistMovies(): Flow<List<SavedMediaItem>>
    fun getWatchlistTvShows(): Flow<List<SavedMediaItem>>
    fun getWatchHistory(): Flow<List<SavedMediaItem>>

    fun getCustomListsFlow(): Flow<List<CustomList>>
    fun getCustomListWithItemsFlow(listId: String): Flow<CustomList?>
    suspend fun createCustomList(
        title: String,
        description: String? = null,
        coverImageUrl: String? = null
    ): String

    suspend fun updateCustomList(listId: String, title: String, description: String?)
    suspend fun deleteCustomList(listId: String)
    suspend fun addMediaToCustomList(
        listId: String,
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String = "",
        voteAvg: Float = 0f,
        userNotes: String? = null
    )

    suspend fun removeMediaFromCustomList(listId: String, mediaId: Int)
    fun getCustomListIdsForMediaFlow(mediaId: Int): Flow<List<String>>
}
