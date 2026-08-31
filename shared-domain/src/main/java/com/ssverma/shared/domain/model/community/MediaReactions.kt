package com.ssverma.shared.domain.model.community

import com.ssverma.shared.domain.model.MediaType

data class MediaReactions(
    val mediaType: MediaType,
    val mediaId: Int,
    val tagCounts: Map<MediaReactionTag, Int> = emptyMap(),
    val totalReactions: Int = 0,
    val userSelectedTags: Set<MediaReactionTag> = emptySet(),
    val isEnabled: Boolean = true
) {
    fun getPercentageForTag(tag: MediaReactionTag): Float {
        if (totalReactions <= 0) return 0f
        val count = tagCounts[tag] ?: 0
        return (count.toFloat() / totalReactions.toFloat()) * 100f
    }

    fun getCountForTag(tag: MediaReactionTag): Int {
        return tagCounts[tag] ?: 0
    }

    fun isTagSelected(tag: MediaReactionTag): Boolean {
        return userSelectedTags.contains(tag)
    }

    companion object {
        fun empty(mediaType: MediaType, mediaId: Int): MediaReactions {
            return MediaReactions(
                mediaType = mediaType,
                mediaId = mediaId,
                tagCounts = emptyMap(),
                totalReactions = 0,
                userSelectedTags = emptySet()
            )
        }
    }
}
