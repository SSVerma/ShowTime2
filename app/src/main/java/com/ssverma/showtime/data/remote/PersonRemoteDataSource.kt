package com.ssverma.showtime.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteImageShot
import com.ssverma.api.service.tmdb.response.RemotePerson

interface PersonRemoteDataSource {

    suspend fun fetchPersonDetails(
        personId: Int,
        queryMap: Map<String, String>
    ): TmdbApiResponse<RemotePerson>

    suspend fun fetchPopularPersonsGradually(
        page: Int
    ): TmdbApiResponse<PagedPayload<RemotePerson>>

    suspend fun fetchPersonImagesGradually(
        personId: Int,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteImageShot>>
}