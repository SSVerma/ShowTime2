package com.ssverma.showtime.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteImageShot
import com.ssverma.api.service.tmdb.response.RemotePerson
import javax.inject.Inject

class DefaultPersonRemoteDataSource @Inject constructor(
    private val tmdbApiService: TmdbApiService
) : PersonRemoteDataSource {

    override suspend fun fetchPersonDetails(
        personId: Int,
        queryMap: Map<String, String>
    ): TmdbApiResponse<RemotePerson> {
        return tmdbApiService.getPersonDetails(
            personId = personId,
            queryMap = queryMap
        )
    }

    override suspend fun fetchPopularPersonsGradually(
        page: Int
    ): TmdbApiResponse<PagedPayload<RemotePerson>> {
        return tmdbApiService.getPopularPersons(page = page)
    }

    override suspend fun fetchPersonImagesGradually(
        personId: Int,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteImageShot>> {
        return tmdbApiService.getPersonTaggedImages(
            personId = personId,
            page = page
        )
    }
}