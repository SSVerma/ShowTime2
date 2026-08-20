package com.ssverma.shared.data.repository

import com.ssverma.shared.data.local.db.dao.CustomListDao
import com.ssverma.shared.data.local.db.dao.CustomListWithItems
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.CustomListEntity
import com.ssverma.shared.data.local.db.entity.CustomListItemEntity
import com.ssverma.shared.data.local.db.entity.FavoriteEntity
import com.ssverma.shared.data.local.db.entity.WatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.model.library.CustomListItem
import com.ssverma.shared.domain.model.library.SavedMediaItem
import com.ssverma.shared.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val watchlistDao: WatchlistDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val customListDao: CustomListDao
) : LibraryRepository {

    override fun isFavoriteFlow(mediaId: Int): Flow<Boolean> {
        return favoriteDao.isFavoriteFlow(mediaId)
    }

    override suspend fun isFavorite(mediaId: Int): Boolean {
        return favoriteDao.isFavorite(mediaId)
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
        val currentlyFavorite = favoriteDao.isFavorite(mediaId)
        if (currentlyFavorite) {
            favoriteDao.deleteFavoriteById(mediaId)
            return false
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    mediaId = mediaId,
                    mediaType = mediaType.toStorageKey(),
                    title = title,
                    posterImageUrl = posterImageUrl,
                    backdropImageUrl = backdropImageUrl,
                    voteAvg = voteAvg,
                    releaseDate = releaseDate,
                    addedAt = System.currentTimeMillis()
                )
            )
            return true
        }
    }

    override suspend fun deleteFavorite(mediaId: Int) {
        favoriteDao.deleteFavoriteById(mediaId)
    }

    override fun isInWatchlistFlow(mediaId: Int): Flow<Boolean> {
        return watchlistDao.isInWatchlistFlow(mediaId)
    }

    override suspend fun isInWatchlist(mediaId: Int): Boolean {
        return watchlistDao.isInWatchlist(mediaId)
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
        val currentlyInWatchlist = watchlistDao.isInWatchlist(mediaId)
        if (currentlyInWatchlist) {
            watchlistDao.deleteWatchlistById(mediaId)
            return false
        } else {
            watchlistDao.insertWatchlist(
                WatchlistEntity(
                    mediaId = mediaId,
                    mediaType = mediaType.toStorageKey(),
                    title = title,
                    posterImageUrl = posterImageUrl,
                    backdropImageUrl = backdropImageUrl,
                    voteAvg = voteAvg,
                    releaseDate = releaseDate,
                    addedAt = System.currentTimeMillis()
                )
            )
            return true
        }
    }

    override suspend fun deleteWatchlist(mediaId: Int) {
        watchlistDao.deleteWatchlistById(mediaId)
    }

    override fun isWatchedFlow(mediaId: Int): Flow<Boolean> {
        return watchHistoryDao.isWatchedFlow(mediaId)
    }

    override suspend fun isWatched(mediaId: Int): Boolean {
        return watchHistoryDao.isWatched(mediaId)
    }

    override suspend fun toggleWatchHistory(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        voteAvg: Float
    ): Boolean {
        val currentlyWatched = watchHistoryDao.isWatched(mediaId)
        if (currentlyWatched) {
            watchHistoryDao.deleteHistoryById(mediaId)
            return false
        } else {
            watchHistoryDao.insertHistory(
                WatchHistoryEntity(
                    mediaId = mediaId,
                    mediaType = mediaType.toStorageKey(),
                    title = title,
                    posterImageUrl = posterImageUrl,
                    voteAvg = voteAvg,
                    watchedAt = System.currentTimeMillis()
                )
            )
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
        watchHistoryDao.insertHistory(
            WatchHistoryEntity(
                mediaId = mediaId,
                mediaType = mediaType.toStorageKey(),
                title = title,
                posterImageUrl = posterImageUrl,
                voteAvg = voteAvg,
                watchedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteWatchHistory(mediaId: Int) {
        watchHistoryDao.deleteHistoryById(mediaId)
    }

    override suspend fun clearWatchHistory() {
        watchHistoryDao.clearHistory()
    }

    override suspend fun clearFavorites() {
        favoriteDao.clearFavorites()
    }

    override suspend fun clearWatchlist() {
        watchlistDao.clearWatchlist()
    }

    override suspend fun clearAllLibrary() {
        favoriteDao.clearFavorites()
        watchlistDao.clearWatchlist()
        watchHistoryDao.clearHistory()
        customListDao.clearAllListItems()
        customListDao.clearAllLists()
    }

    override fun getAllFavorites(): Flow<List<SavedMediaItem>> {
        return favoriteDao.getAllFavoritesFlow().map { list ->
            list.map { it.toSavedMediaItem() }
        }
    }

    override fun getAllWatchlist(): Flow<List<SavedMediaItem>> {
        return watchlistDao.getAllWatchlistFlow().map { list ->
            list.map { it.toSavedMediaItem() }
        }
    }

    override fun getAllWatchHistory(): Flow<List<SavedMediaItem>> {
        return watchHistoryDao.getAllHistoryFlow().map { list ->
            list.map { it.toSavedMediaItem() }
        }
    }

    override fun getFavoriteMovies(): Flow<List<SavedMediaItem>> {
        return favoriteDao.getFavoritesByTypeFlow("movie").map { list ->
            list.map { it.toSavedMediaItem() }
        }
    }

    override fun getFavoriteTvShows(): Flow<List<SavedMediaItem>> {
        return favoriteDao.getFavoritesByTypeFlow("tv").map { list ->
            list.map { it.toSavedMediaItem() }
        }
    }

    override fun getWatchlistMovies(): Flow<List<SavedMediaItem>> {
        return watchlistDao.getWatchlistByTypeFlow("movie").map { list ->
            list.map { it.toSavedMediaItem() }
        }
    }

    override fun getWatchlistTvShows(): Flow<List<SavedMediaItem>> {
        return watchlistDao.getWatchlistByTypeFlow("tv").map { list ->
            list.map { it.toSavedMediaItem() }
        }
    }

    override fun getWatchHistory(): Flow<List<SavedMediaItem>> {
        return watchHistoryDao.getAllHistoryFlow().map { list ->
            list.map { it.toSavedMediaItem() }
        }
    }

    override fun getCustomListsFlow(): Flow<List<CustomList>> {
        return customListDao.getAllListsWithItemsFlow().map { lists ->
            lists.map { it.toCustomList() }
        }
    }

    override fun getCustomListWithItemsFlow(listId: String): Flow<CustomList?> {
        return customListDao.getListWithItemsFlow(listId).map { it?.toCustomList() }
    }

    override suspend fun createCustomList(
        title: String,
        description: String?,
        coverImageUrl: String?
    ): String {
        val listId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        customListDao.insertList(
            CustomListEntity(
                listId = listId,
                title = title,
                description = description,
                coverImageUrl = coverImageUrl,
                isPublic = false,
                createdAt = now,
                updatedAt = now
            )
        )
        return listId
    }

    override suspend fun updateCustomList(
        listId: String,
        title: String,
        description: String?
    ) {
        customListDao.updateList(
            listId = listId,
            title = title,
            description = description,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun deleteCustomList(listId: String) {
        customListDao.deleteListById(listId)
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
        customListDao.insertListItem(
            CustomListItemEntity(
                listId = listId,
                mediaId = mediaId,
                mediaType = mediaType.toStorageKey(),
                title = title,
                posterImageUrl = posterImageUrl,
                backdropImageUrl = backdropImageUrl,
                voteAvg = voteAvg,
                userNotes = userNotes,
                rankOrder = 0,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun removeMediaFromCustomList(listId: String, mediaId: Int) {
        customListDao.deleteListItem(listId, mediaId)
    }

    override fun getCustomListIdsForMediaFlow(mediaId: Int): Flow<List<String>> {
        return customListDao.getListIdsForMediaFlow(mediaId)
    }

    private fun CustomListWithItems.toCustomList(): CustomList {
        return CustomList(
            listId = list.listId,
            title = list.title,
            description = list.description,
            coverImageUrl = list.coverImageUrl,
            isPublic = list.isPublic,
            items = items.map { it.toCustomListItem() },
            createdAt = list.createdAt,
            updatedAt = list.updatedAt
        )
    }

    private fun CustomListItemEntity.toCustomListItem(): CustomListItem {
        return CustomListItem(
            listId = listId,
            mediaId = mediaId,
            mediaType = mediaType.toMediaType(),
            title = title,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = backdropImageUrl,
            voteAvg = voteAvg,
            userNotes = userNotes,
            rankOrder = rankOrder,
            addedAt = addedAt
        )
    }

    private fun FavoriteEntity.toSavedMediaItem() = SavedMediaItem(
        mediaId = mediaId,
        mediaType = mediaType.toMediaType(),
        title = title,
        posterImageUrl = posterImageUrl,
        backdropImageUrl = backdropImageUrl,
        voteAvg = voteAvg,
        releaseDate = releaseDate,
        addedAt = addedAt
    )

    private fun WatchlistEntity.toSavedMediaItem() = SavedMediaItem(
        mediaId = mediaId,
        mediaType = mediaType.toMediaType(),
        title = title,
        posterImageUrl = posterImageUrl,
        backdropImageUrl = backdropImageUrl,
        voteAvg = voteAvg,
        releaseDate = releaseDate,
        addedAt = addedAt
    )

    private fun WatchHistoryEntity.toSavedMediaItem() = SavedMediaItem(
        mediaId = mediaId,
        mediaType = mediaType.toMediaType(),
        title = title,
        posterImageUrl = posterImageUrl,
        backdropImageUrl = "",
        voteAvg = voteAvg,
        releaseDate = "",
        addedAt = watchedAt
    )

    private fun MediaType.toStorageKey(): String = when (this) {
        MediaType.Movie -> "movie"
        MediaType.Tv -> "tv"
        MediaType.Person -> "person"
        MediaType.Unknown -> "unknown"
    }

    private fun String.toMediaType(): MediaType = when (this.lowercase()) {
        "movie" -> MediaType.Movie
        "tv" -> MediaType.Tv
        "person" -> MediaType.Person
        else -> MediaType.Unknown
    }
}

