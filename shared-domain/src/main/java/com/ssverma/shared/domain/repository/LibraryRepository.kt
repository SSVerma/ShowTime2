package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.MediaType
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun isFavoriteFlow(mediaId: Int): Flow<Boolean>
    suspend fun isFavorite(mediaId: Int): Boolean
    suspend fun toggleFavorite(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String,
        voteAvg: Float,
        releaseDate: String
    ): Boolean

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

    suspend fun logWatchHistory(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        voteAvg: Float
    )
}
