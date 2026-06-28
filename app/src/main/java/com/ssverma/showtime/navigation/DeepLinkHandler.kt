package com.ssverma.showtime.navigation

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.person.navigation.PersonDetailNavKey
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey

object ShowTimeDeepLinkHandler {
    private const val TAG = "DeepLinkHandler"
    private const val HOST = "www.ssverma.in"
    private const val SCHEME = "showtime"

    fun parse(uri: Uri): NavKey? {
        Log.d(TAG, "Parsing deep link: $uri")

        val scheme = uri.scheme
        val host = uri.host

        if (!scheme.equals(SCHEME, ignoreCase = true) || !host.equals(HOST, ignoreCase = true)) {
            Log.w(TAG, "Invalid scheme or host. Expected: $SCHEME://$HOST, Actual: $scheme://$host")
            return null
        }

        val pathSegments = uri.pathSegments
        if (pathSegments.size < 2) {
            Log.w(TAG, "Insufficient path segments: $pathSegments")
            return null
        }

        val type = pathSegments[0]
        val id = pathSegments[1].toIntOrNull()

        if (id == null) {
            Log.w(TAG, "Invalid ID in path: ${pathSegments[1]}")
            return null
        }

        return try {
            when {
                type.equals("movie", ignoreCase = true) -> {
                    MovieDetailNavKey(id)
                }

                type.equals("tv", ignoreCase = true) -> {
                    TvShowDetailNavKey(id)
                }

                type.equals("person", ignoreCase = true) -> {
                    PersonDetailNavKey(id)
                }

                else -> {
                    Log.w(TAG, "Unsupported type: $type")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing deep link", e)
            null
        }
    }

    fun parse(uriString: String?): NavKey? {
        if (uriString.isNullOrBlank()) return null
        return try {
            parse(uriString.toUri())
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing deep link string: $uriString", e)
            null
        }
    }
}
