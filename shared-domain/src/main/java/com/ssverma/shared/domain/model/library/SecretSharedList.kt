package com.ssverma.shared.domain.model.library

import com.ssverma.shared.domain.model.MediaType

data class SecretSharedList(
    val shareCode: String,
    val title: String,
    val description: String? = null,
    val ownerUserId: String,
    val ownerName: String,
    val isCollaborative: Boolean = false,
    val isRevoked: Boolean = false,
    val items: List<SecretSharedListItem> = emptyList(),
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
) {
    val itemCount: Int get() = items.size
    val previewPosters: List<String>
        get() = items.map { it.posterImageUrl }.filter { it.isNotBlank() }.take(6)
    val averageRating: Float
        get() {
            val ratedItems = items.filter { it.voteAvg > 0f }
            return if (ratedItems.isNotEmpty()) {
                ratedItems.map { it.voteAvg }.average().toFloat()
            } else {
                0f
            }
        }
}

data class SecretSharedListItem(
    val mediaId: Int,
    val mediaType: MediaType,
    val title: String,
    val posterImageUrl: String,
    val backdropImageUrl: String = "",
    val voteAvg: Float = 0f,
    val releaseYear: String? = null,
    val overview: String? = null,
    val addedByName: String? = null,
    val addedByUserId: String? = null,
    val addedAtEpochMs: Long = System.currentTimeMillis()
)
