package com.ssverma.shared.data.repository

import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.FavoriteEntity
import com.ssverma.shared.data.local.db.entity.WatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val watchlistDao: WatchlistDao,
    private val watchHistoryDao: WatchHistoryDao
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

    private fun MediaType.toStorageKey(): String = when (this) {
        MediaType.Movie -> "movie"
        MediaType.Tv -> "tv"
        MediaType.Person -> "person"
        MediaType.Unknown -> "unknown"
    }
}
