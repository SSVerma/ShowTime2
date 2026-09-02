package com.ssverma.showtime.navigation

import android.net.Uri
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import com.ssverma.feature.library.navigation.BacklogChallengeNavKey
import com.ssverma.feature.library.navigation.CinemaDiaryNavKey
import com.ssverma.feature.library.navigation.CinemaReceiptNavKey
import com.ssverma.feature.library.navigation.CinephileWrappedNavKey
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.library.navigation.LibraryTabDestination
import com.ssverma.feature.library.navigation.TasteProfileNavKey
import com.ssverma.feature.movie.navigation.CinemaGameNavKey
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.movie.navigation.MovieHomeNavKey
import com.ssverma.feature.person.navigation.PersonDetailNavKey
import com.ssverma.feature.person.navigation.PersonHomeNavKey
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey
import com.ssverma.feature.tv.navigation.TvShowHomeNavKey
import com.ssverma.showtime.feature.filter.navigation.UniversalDiscoveryNavKey

object ShowTimeDeepLinkHandler {
    const val PRIMARY_HOST = "showtime.ssverma.in"
    private val ALLOWED_HOSTS = setOf(
        "showtime.ssverma.in",
        "www.ssverma.in",
        "ssverma.in"
    )
    private val ALLOWED_SCHEMES = setOf("showtime", "https", "http")

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
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.lowercase())) {
            return null
        }
        if (host == null || !ALLOWED_HOSTS.contains(host.lowercase())) {
            return null
        }

        val effectiveSegments = if (pathSegments.isNotEmpty() && pathSegments[0].equals(
                "showtime",
                ignoreCase = true
            )
        ) {
            pathSegments.drop(1)
        } else {
            pathSegments
        }

        if (effectiveSegments.isEmpty()) {
            return DashboardHomeNavKey
        }

        val type = effectiveSegments[0].lowercase()

        return try {
            when (type) {
                "home", "dashboard" -> DashboardHomeNavKey

                "game", "puzzle" -> CinemaGameNavKey

                "challenges", "challenge", "backlog", "blindspot", "blindspots" -> BacklogChallengeNavKey

                "search" -> SearchNavKey

                "discover", "discovery", "browse" -> {
                    val vibe = if (effectiveSegments.size >= 2) effectiveSegments[1] else "ALL"
                    UniversalDiscoveryNavKey(initialVibe = vibe)
                }

                "receipt", "receipts" -> CinemaReceiptNavKey

                "wrapped", "milestones" -> CinephileWrappedNavKey

                "taste", "recommendations" -> TasteProfileNavKey

                "diary" -> CinemaDiaryNavKey

                "lists", "list" -> {
                    if (effectiveSegments.size >= 2) {
                        LibraryHomeNavKey(
                            initialTab = LibraryTabDestination.Community,
                            targetCustomListId = effectiveSegments[1]
                        )
                    } else {
                        LibraryHomeNavKey(initialTab = LibraryTabDestination.Community)
                    }
                }

                "community" -> {
                    if (effectiveSegments.size >= 2) {
                        LibraryHomeNavKey(
                            initialTab = LibraryTabDestination.Community,
                            targetCustomListId = effectiveSegments[1]
                        )
                    } else {
                        LibraryHomeNavKey(initialTab = LibraryTabDestination.Community)
                    }
                }

                "library" -> {
                    val subTab =
                        if (effectiveSegments.size > 1) effectiveSegments[1].lowercase() else "watchlist"
                    when (subTab) {
                        "favorites", "favorite" -> LibraryHomeNavKey(initialTab = LibraryTabDestination.Favorites)
                        "history" -> LibraryHomeNavKey(initialTab = LibraryTabDestination.History)
                        "custom_lists", "lists", "my_lists" -> {
                            if (effectiveSegments.size >= 3) {
                                LibraryHomeNavKey(
                                    initialTab = LibraryTabDestination.CustomLists,
                                    targetCustomListId = effectiveSegments[2]
                                )
                            } else {
                                LibraryHomeNavKey(initialTab = LibraryTabDestination.CustomLists)
                            }
                        }

                        "community", "explore", "community_lists" -> {
                            if (effectiveSegments.size >= 3) {
                                LibraryHomeNavKey(
                                    initialTab = LibraryTabDestination.Community,
                                    targetCustomListId = effectiveSegments[2]
                                )
                            } else {
                                LibraryHomeNavKey(initialTab = LibraryTabDestination.Community)
                            }
                        }

                        else -> LibraryHomeNavKey(initialTab = LibraryTabDestination.Watchlist)
                    }
                }

                "tv" -> {
                    if (effectiveSegments.size >= 2) {
                        val id = effectiveSegments[1].toIntOrNull()
                        if (id != null) TvShowDetailNavKey(id) else TvShowHomeNavKey
                    } else {
                        TvShowHomeNavKey
                    }
                }

                "movie" -> {
                    if (effectiveSegments.size >= 2) {
                        val id = effectiveSegments[1].toIntOrNull()
                        if (id != null) MovieDetailNavKey(id) else MovieHomeNavKey
                    } else {
                        MovieHomeNavKey
                    }
                }

                "person", "people" -> {
                    if (effectiveSegments.size >= 2) {
                        val id = effectiveSegments[1].toIntOrNull()
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

