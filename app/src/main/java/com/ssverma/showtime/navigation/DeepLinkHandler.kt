package com.ssverma.showtime.navigation

import android.net.Uri
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.library.navigation.LibraryTabDestination
import com.ssverma.feature.movie.navigation.CinemaGameNavKey
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.movie.navigation.MovieHomeNavKey
import com.ssverma.feature.person.navigation.PersonDetailNavKey
import com.ssverma.feature.person.navigation.PersonHomeNavKey
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey
import com.ssverma.feature.tv.navigation.TvShowHomeNavKey

object ShowTimeDeepLinkHandler {
    private const val HOST = "www.ssverma.in"
    private const val SCHEME = "showtime"

    fun parse(uri: Uri): NavKey? {
        return parseParts(uri.scheme, uri.host, uri.pathSegments)
    }

    fun parse(uriString: String?): NavKey? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val javaUri = java.net.URI(uriString)
            val segments = javaUri.path?.split("/")?.filter { it.isNotEmpty() } ?: emptyList()
            parseParts(javaUri.scheme, javaUri.host, segments)
        } catch (_: Exception) {
            null
        }
    }

    fun parseParts(scheme: String?, host: String?, pathSegments: List<String>): NavKey? {
        if (!scheme.equals(SCHEME, ignoreCase = true) || !host.equals(HOST, ignoreCase = true)) {
            return null
        }

        if (pathSegments.isEmpty()) {
            return DashboardHomeNavKey
        }

        val type = pathSegments[0].lowercase()

        return try {
            when (type) {
                "home", "dashboard" -> DashboardHomeNavKey

                "game", "challenge" -> CinemaGameNavKey

                "search" -> SearchNavKey

                "community" -> LibraryHomeNavKey(initialTab = LibraryTabDestination.Community)

                "library" -> {
                    val subTab =
                        if (pathSegments.size > 1) pathSegments[1].lowercase() else "watchlist"
                    when (subTab) {
                        "favorites", "favorite" -> LibraryHomeNavKey(initialTab = LibraryTabDestination.Favorites)
                        "history" -> LibraryHomeNavKey(initialTab = LibraryTabDestination.History)
                        "custom_lists", "lists", "my_lists" -> LibraryHomeNavKey(initialTab = LibraryTabDestination.CustomLists)
                        "community", "explore", "community_lists" -> LibraryHomeNavKey(initialTab = LibraryTabDestination.Community)
                        else -> LibraryHomeNavKey(initialTab = LibraryTabDestination.Watchlist)
                    }
                }

                "tv" -> {
                    if (pathSegments.size >= 2) {
                        val id = pathSegments[1].toIntOrNull()
                        if (id != null) TvShowDetailNavKey(id) else TvShowHomeNavKey
                    } else {
                        TvShowHomeNavKey
                    }
                }

                "movie" -> {
                    if (pathSegments.size >= 2) {
                        val id = pathSegments[1].toIntOrNull()
                        if (id != null) MovieDetailNavKey(id) else MovieHomeNavKey
                    } else {
                        MovieHomeNavKey
                    }
                }

                "person", "people" -> {
                    if (pathSegments.size >= 2) {
                        val id = pathSegments[1].toIntOrNull()
                        if (id != null) PersonDetailNavKey(id) else PersonHomeNavKey
                    } else {
                        PersonHomeNavKey
                    }
                }

                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}
