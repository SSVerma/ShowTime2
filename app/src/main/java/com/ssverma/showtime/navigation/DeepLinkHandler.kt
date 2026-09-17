package com.ssverma.showtime.navigation

import androidx.navigation3.runtime.NavKey
import com.ssverma.feature.library.navigation.BacklogChallengeNavKey
import com.ssverma.feature.library.navigation.ChallengeDetailNavKey
import com.ssverma.feature.library.navigation.CinemaDiaryNavKey
import com.ssverma.feature.library.navigation.CinemaReceiptNavKey
import com.ssverma.feature.library.navigation.CinephileWrappedNavKey
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.library.navigation.LibraryTabDestination
import com.ssverma.feature.library.navigation.SecretSharedListNavKey
import com.ssverma.feature.library.navigation.TasteProfileNavKey
import com.ssverma.feature.match.navigation.MatchRoomNavKey
import com.ssverma.feature.movie.navigation.CinemaGameNavKey
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.movie.navigation.MovieHomeNavKey
import com.ssverma.feature.person.navigation.PersonDetailNavKey
import com.ssverma.feature.person.navigation.PersonHomeNavKey
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey
import com.ssverma.feature.tv.navigation.TvShowHomeNavKey
import com.ssverma.showtime.feature.filter.navigation.UniversalDiscoveryNavKey
import java.net.URI

object ShowTimeDeepLinkHandler {

    // Schemes
    const val SCHEME_SHOWTIME = "showtime"
    const val SCHEME_HTTPS = "https"
    const val SCHEME_HTTP = "http"

    // Hosts
    const val PRIMARY_HOST = "showtime.ssverma.in"
    const val HOST_PRIMARY = PRIMARY_HOST
    const val HOST_WWW = "www.ssverma.in"
    const val HOST_ROOT = "ssverma.in"

    // Path Prefixes & Special Tokens
    private const val PREFIX_SHOWTIME = "showtime"
    private const val PREFIX_SECRET_LIST = "SL-"
    private const val DEFAULT_DISCOVERY_VIBE = "ALL"

    // Path Keywords
    private const val PATH_HOME = "home"
    private const val PATH_DASHBOARD = "dashboard"

    private const val PATH_GAME = "game"
    private const val PATH_PUZZLE = "puzzle"

    private const val PATH_CHALLENGES = "challenges"
    private const val PATH_CHALLENGE = "challenge"
    private const val PATH_BACKLOG = "backlog"
    private const val PATH_BLINDSPOT = "blindspot"
    private const val PATH_BLINDSPOTS = "blindspots"

    private const val PATH_SEARCH = "search"

    private const val PATH_DISCOVER = "discover"
    private const val PATH_DISCOVERY = "discovery"
    private const val PATH_BROWSE = "browse"

    private const val PATH_RECEIPT = "receipt"
    private const val PATH_RECEIPTS = "receipts"

    private const val PATH_WRAPPED = "wrapped"
    private const val PATH_MILESTONES = "milestones"

    private const val PATH_TASTE = "taste"
    private const val PATH_RECOMMENDATIONS = "recommendations"

    private const val PATH_DIARY = "diary"

    private const val PATH_SECRET_LIST_SHORT = "l"
    private const val PATH_SECRET_LIST = "secret_list"
    private const val PATH_SHARED_LIST = "shared_list"

    private const val PATH_LISTS = "lists"
    private const val PATH_LIST = "list"

    private const val PATH_COMMUNITY = "community"
    private const val PATH_LIBRARY = "library"

    private const val PATH_TV = "tv"
    private const val PATH_MOVIE = "movie"
    private const val PATH_PERSON = "person"
    private const val PATH_PEOPLE = "people"

    private const val PATH_MATCH = "match"
    private const val PATH_MATCHROOM = "matchroom"
    private const val PATH_SWIPENIGHT = "swipenight"

    // Library Sub-tab Keywords
    private const val TAB_WATCHLIST = "watchlist"
    private const val TAB_FAVORITES = "favorites"
    private const val TAB_FAVORITE = "favorite"
    private const val TAB_HISTORY = "history"
    private const val TAB_CUSTOM_LISTS = "custom_lists"
    private const val TAB_MY_LISTS = "my_lists"
    private const val TAB_EXPLORE = "explore"
    private const val TAB_COMMUNITY_LISTS = "community_lists"

    private val ALLOWED_HOSTS = setOf(
        HOST_PRIMARY,
        HOST_WWW,
        HOST_ROOT
    )

    private val ALLOWED_SCHEMES = setOf(
        SCHEME_SHOWTIME,
        SCHEME_HTTPS,
        SCHEME_HTTP
    )

    fun parse(uriString: String?): NavKey? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val javaUri = URI(uriString)
            val segments = javaUri.path?.split("/")?.filter { it.isNotEmpty() } ?: emptyList()
            parseParts(
                scheme = javaUri.scheme,
                host = javaUri.host,
                pathSegments = segments
            )
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
                PREFIX_SHOWTIME,
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
                PATH_HOME, PATH_DASHBOARD -> DashboardHomeNavKey

                PATH_GAME, PATH_PUZZLE -> CinemaGameNavKey

                PATH_CHALLENGES, PATH_CHALLENGE, PATH_BACKLOG, PATH_BLINDSPOT, PATH_BLINDSPOTS -> {
                    if (effectiveSegments.size >= 2 && effectiveSegments[1].isNotBlank()) {
                        ChallengeDetailNavKey(challengeId = effectiveSegments[1])
                    } else {
                        BacklogChallengeNavKey
                    }
                }

                PATH_SEARCH -> SearchNavKey

                PATH_DISCOVER, PATH_DISCOVERY, PATH_BROWSE -> {
                    val vibe =
                        if (effectiveSegments.size >= 2) effectiveSegments[1] else DEFAULT_DISCOVERY_VIBE
                    UniversalDiscoveryNavKey(initialVibe = vibe)
                }

                PATH_RECEIPT, PATH_RECEIPTS -> CinemaReceiptNavKey

                PATH_WRAPPED, PATH_MILESTONES -> CinephileWrappedNavKey

                PATH_TASTE, PATH_RECOMMENDATIONS -> TasteProfileNavKey

                PATH_DIARY -> CinemaDiaryNavKey

                PATH_SECRET_LIST_SHORT, PATH_SECRET_LIST, PATH_SHARED_LIST -> {
                    if (effectiveSegments.size >= 2) {
                        SecretSharedListNavKey(shareCode = effectiveSegments[1].uppercase())
                    } else {
                        LibraryHomeNavKey(initialTab = LibraryTabDestination.CustomLists)
                    }
                }

                PATH_LISTS, PATH_LIST -> {
                    if (effectiveSegments.size >= 2) {
                        val segment = effectiveSegments[1]
                        if (segment.startsWith(PREFIX_SECRET_LIST, ignoreCase = true)) {
                            SecretSharedListNavKey(shareCode = segment.uppercase())
                        } else {
                            LibraryHomeNavKey(
                                initialTab = LibraryTabDestination.Community,
                                targetCustomListId = segment
                            )
                        }
                    } else {
                        LibraryHomeNavKey(initialTab = LibraryTabDestination.Community)
                    }
                }

                PATH_COMMUNITY -> {
                    if (effectiveSegments.size >= 2) {
                        LibraryHomeNavKey(
                            initialTab = LibraryTabDestination.Community,
                            targetCustomListId = effectiveSegments[1]
                        )
                    } else {
                        LibraryHomeNavKey(initialTab = LibraryTabDestination.Community)
                    }
                }

                PATH_LIBRARY -> {
                    val subTab = if (effectiveSegments.size > 1) {
                        effectiveSegments[1].lowercase()
                    } else {
                        TAB_WATCHLIST
                    }
                    when (subTab) {
                        TAB_FAVORITES, TAB_FAVORITE -> {
                            LibraryHomeNavKey(initialTab = LibraryTabDestination.Favorites)
                        }

                        TAB_HISTORY -> {
                            LibraryHomeNavKey(initialTab = LibraryTabDestination.History)
                        }

                        TAB_CUSTOM_LISTS, PATH_LISTS, TAB_MY_LISTS -> {
                            if (effectiveSegments.size >= 3) {
                                LibraryHomeNavKey(
                                    initialTab = LibraryTabDestination.CustomLists,
                                    targetCustomListId = effectiveSegments[2]
                                )
                            } else {
                                LibraryHomeNavKey(initialTab = LibraryTabDestination.CustomLists)
                            }
                        }

                        PATH_COMMUNITY, TAB_EXPLORE, TAB_COMMUNITY_LISTS -> {
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

                PATH_TV -> {
                    if (effectiveSegments.size >= 2) {
                        val id = effectiveSegments[1].toIntOrNull()
                        if (id != null) TvShowDetailNavKey(tvShowId = id) else TvShowHomeNavKey
                    } else {
                        TvShowHomeNavKey
                    }
                }

                PATH_MATCH, PATH_MATCHROOM, PATH_SWIPENIGHT -> {
                    val code = if (effectiveSegments.size >= 2) effectiveSegments[1] else null
                    MatchRoomNavKey(roomCode = code)
                }

                PATH_MOVIE -> {
                    if (effectiveSegments.size >= 2) {
                        if (effectiveSegments[1].equals(PATH_MATCH, ignoreCase = true) ||
                            effectiveSegments[1].equals(PATH_MATCHROOM, ignoreCase = true)
                        ) {
                            val code =
                                if (effectiveSegments.size >= 3) effectiveSegments[2] else null
                            MatchRoomNavKey(roomCode = code)
                        } else {
                            val id = effectiveSegments[1].toIntOrNull()
                            if (id != null) MovieDetailNavKey(movieId = id) else MovieHomeNavKey
                        }
                    } else {
                        MovieHomeNavKey
                    }
                }

                PATH_PERSON, PATH_PEOPLE -> {
                    if (effectiveSegments.size >= 2) {
                        val id = effectiveSegments[1].toIntOrNull()
                        if (id != null) PersonDetailNavKey(personId = id) else PersonHomeNavKey
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

