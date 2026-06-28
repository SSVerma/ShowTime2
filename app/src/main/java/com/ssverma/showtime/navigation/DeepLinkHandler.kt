package com.ssverma.showtime.navigation

import android.net.Uri
import androidx.navigation3.runtime.NavKey
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey

object ShowTimeDeepLinkHandler {
    fun parse(uri: Uri): NavKey? {
        return when {
            uri.path?.startsWith("/movie/") == true -> {
                uri.lastPathSegment?.toIntOrNull()?.let { MovieDetailNavKey(it) }
            }
            uri.path?.startsWith("/tv/") == true -> {
                uri.lastPathSegment?.toIntOrNull()?.let { TvShowDetailNavKey(it) }
            }
            else -> null
        }
    }
}
