package com.ssverma.feature.account.data.mapper

import com.ssverma.api.service.tmdb.response.MediaRating
import com.ssverma.api.service.tmdb.response.MediaStatsPayload
import com.ssverma.feature.account.domain.model.MediaStats
import com.ssverma.shared.data.mapper.Mapper
import javax.inject.Inject

class MediaStatsMapper @Inject constructor() : Mapper<MediaStatsPayload, MediaStats>() {
    override suspend fun map(input: MediaStatsPayload): MediaStats {
        return input.asMediaStats()
    }
}

private fun MediaStatsPayload.asMediaStats(): MediaStats {
    return MediaStats(
        mediaId = mediaId,
        favorite = favorite,
        inWatchlist = inWatchlist,
        rating = (rating as? MediaRating)?.value
    )
}