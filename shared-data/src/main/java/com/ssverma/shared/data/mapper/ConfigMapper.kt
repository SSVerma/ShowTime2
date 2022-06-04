package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.AppendableQueryMap
import com.ssverma.api.service.tmdb.QueryMultiValue
import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.shared.domain.model.MediaDetailsAppendable
import com.ssverma.shared.domain.model.PersonDetailAppendable

fun List<MediaDetailsAppendable>.asQueryMap(): Map<String, String> {
    val queryBuilder = QueryMultiValue.andBuilder()

    this.forEach { appendable ->
        when (appendable) {
            MediaDetailsAppendable.Credits -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Credits)
            }
            MediaDetailsAppendable.Images -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Images)
            }
            MediaDetailsAppendable.Keywords -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Keywords)
            }
            MediaDetailsAppendable.Lists -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Lists)
            }
            MediaDetailsAppendable.Recommendations -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Recommendations)
            }
            MediaDetailsAppendable.Reviews -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Reviews)
            }
            MediaDetailsAppendable.Similar -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Similar)
            }
            MediaDetailsAppendable.Videos -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Videos)
            }
        }
    }

    return AppendableQueryMap.of(queryBuilder)
}

@JvmName("asQueryMapPersonDetailAppendable")
fun List<PersonDetailAppendable>.asQueryMap(): Map<String, String> {
    val queryBuilder = QueryMultiValue.andBuilder()

    this.forEach { appendable ->
        when (appendable) {
            PersonDetailAppendable.CombinedCredits -> {
                queryBuilder.and(TmdbApiTiedConstants.PersonDetailsAppendableResponseTypes.Credits)
            }
            PersonDetailAppendable.Images -> {
                queryBuilder.and(TmdbApiTiedConstants.PersonDetailsAppendableResponseTypes.Images)
            }
        }
    }

    return AppendableQueryMap.of(queryBuilder)
}