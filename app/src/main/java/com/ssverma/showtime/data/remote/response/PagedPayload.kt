package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

@Deprecated("use -> api.service.tmdb")
class PagedPayload<T>(
    @SerializedName("id")
    val id: Int,

    @SerializedName("page")
    val page: Int,

    @SerializedName("total_pages")
    val pageCount: Int,

    @SerializedName("total_results")
    val resultCount: Int,

    @SerializedName("results")
    val results: List<T>?
)