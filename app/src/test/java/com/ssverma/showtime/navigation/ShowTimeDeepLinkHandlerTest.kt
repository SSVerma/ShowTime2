package com.ssverma.showtime.navigation

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShowTimeDeepLinkHandlerTest {

    @Test
    fun parse_rootDeepLink_returnsDashboardHomeNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("showtime://showtime.ssverma.in")
        assertEquals(DashboardHomeNavKey, navKey)
    }

    @Test
    fun parse_wrappedDeepLink_returnsCinephileWrappedNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/wrapped")
        assertEquals(CinephileWrappedNavKey, navKey)
    }

    @Test
    fun parse_tasteDeepLink_returnsTasteProfileNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/taste")
        assertEquals(TasteProfileNavKey, navKey)
    }

    @Test
    fun parse_diaryDeepLink_returnsCinemaDiaryNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/diary")
        assertEquals(CinemaDiaryNavKey, navKey)
    }

    @Test
    fun parse_httpsRootDeepLink_returnsDashboardHomeNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in")
        assertEquals(DashboardHomeNavKey, navKey)
    }

    @Test
    fun parse_homeDeepLink_returnsDashboardHomeNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("showtime://showtime.ssverma.in/home")
        assertEquals(DashboardHomeNavKey, navKey)
    }

    @Test
    fun parse_gameDeepLink_returnsCinemaGameNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/game")
        assertEquals(CinemaGameNavKey, navKey)
    }

    @Test
    fun parse_searchDeepLink_returnsSearchNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/search")
        assertEquals(SearchNavKey, navKey)
    }

    @Test
    fun parse_receiptDeepLink_returnsCinemaReceiptNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/receipt")
        assertEquals(CinemaReceiptNavKey, navKey)
    }

    @Test
    fun parse_peopleDeepLink_returnsPersonHomeNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("showtime://showtime.ssverma.in/people")
        assertEquals(PersonHomeNavKey, navKey)
    }

    @Test
    fun parse_libraryWatchlistDeepLink_returnsLibraryHomeNavKeyWithWatchlist() {
        val navKey =
            ShowTimeDeepLinkHandler.parse("showtime://showtime.ssverma.in/library/watchlist")
        assertEquals(LibraryHomeNavKey(initialTab = LibraryTabDestination.Watchlist), navKey)
    }

    @Test
    fun parse_libraryFavoritesDeepLink_returnsLibraryHomeNavKeyWithFavorites() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/library/favorites")
        assertEquals(LibraryHomeNavKey(initialTab = LibraryTabDestination.Favorites), navKey)
    }

    @Test
    fun parse_libraryRootDeepLink_returnsLibraryHomeNavKeyWithDefaultWatchlist() {
        val navKey = ShowTimeDeepLinkHandler.parse("showtime://showtime.ssverma.in/library")
        assertEquals(LibraryHomeNavKey(initialTab = LibraryTabDestination.Watchlist), navKey)
    }

    @Test
    fun parse_libraryCommunityDeepLink_returnsLibraryHomeNavKeyWithCommunity() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/library/community")
        assertEquals(LibraryHomeNavKey(initialTab = LibraryTabDestination.Community), navKey)
    }

    @Test
    fun parse_communityDirectDeepLink_returnsLibraryHomeNavKeyWithCommunity() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/community")
        assertEquals(LibraryHomeNavKey(initialTab = LibraryTabDestination.Community), navKey)
    }

    @Test
    fun parse_communityListDetailDeepLink_returnsLibraryHomeNavKeyWithTargetListId() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/lists/list_999")
        assertEquals(
            LibraryHomeNavKey(
                initialTab = LibraryTabDestination.Community,
                targetCustomListId = "list_999"
            ),
            navKey
        )
    }

    @Test
    fun parse_tvHomeDeepLink_returnsTvShowHomeNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/tv")
        assertEquals(TvShowHomeNavKey, navKey)
    }

    @Test
    fun parse_tvShowDetailDeepLink_returnsTvShowDetailNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/tv/1399")
        assertEquals(TvShowDetailNavKey(1399), navKey)
    }

    @Test
    fun parse_movieHomeDeepLink_returnsMovieHomeNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/movie")
        assertEquals(MovieHomeNavKey, navKey)
    }

    @Test
    fun parse_movieDetailDeepLink_returnsMovieDetailNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/movie/550")
        assertEquals(MovieDetailNavKey(550), navKey)
    }

    @Test
    fun parse_personDetailDeepLink_returnsPersonDetailNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/person/287")
        assertEquals(PersonDetailNavKey(287), navKey)
    }

    @Test
    fun parse_legacyHostWithShowTimePrefix_returnsParsedNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://www.ssverma.in/showtime/movie/550")
        assertEquals(MovieDetailNavKey(550), navKey)
    }

    @Test
    fun parse_challengesDeepLink_returnsBacklogChallengeNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/challenges")
        assertEquals(BacklogChallengeNavKey, navKey)
    }

    @Test
    fun parse_backlogDeepLink_returnsBacklogChallengeNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("showtime://showtime.ssverma.in/backlog")
        assertEquals(BacklogChallengeNavKey, navKey)
    }

    @Test
    fun parse_discoverDeepLink_returnsUniversalDiscoveryNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/discover")
        assertEquals(UniversalDiscoveryNavKey(initialVibe = "ALL"), navKey)
    }

    @Test
    fun parse_discoverVibeDeepLink_returnsUniversalDiscoveryNavKeyWithVibe() {
        val navKey =
            ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/discover/MIND_BENDING")
        assertEquals(UniversalDiscoveryNavKey(initialVibe = "MIND_BENDING"), navKey)
    }

    @Test
    fun parse_invalidHost_returnsNull() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://www.google.com/tv/1399")
        assertNull(navKey)
    }

    @Test
    fun parse_secretListShortDeepLink_returnsSecretSharedListNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/l/4821")
        assertEquals(SecretSharedListNavKey("4821"), navKey)
    }

    @Test
    fun parse_secretListPrefixedDeepLink_returnsSecretSharedListNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/list/SL-4821")
        assertEquals(SecretSharedListNavKey("SL-4821"), navKey)
    }

    @Test
    fun parse_nullOrBlankUri_returnsNull() {
        assertNull(ShowTimeDeepLinkHandler.parse(null))
        assertNull(ShowTimeDeepLinkHandler.parse(""))
        assertNull(ShowTimeDeepLinkHandler.parse("   "))
    }

    @Test
    fun parse_invalidScheme_returnsNull() {
        val navKey = ShowTimeDeepLinkHandler.parse("ftp://showtime.ssverma.in/movie/550")
        assertNull(navKey)
    }

    @Test
    fun parse_puzzleDeepLink_returnsCinemaGameNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/puzzle")
        assertEquals(CinemaGameNavKey, navKey)
    }

    @Test
    fun parse_challengeDetailDeepLink_returnsChallengeDetailNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/challenges/c_123")
        assertEquals(ChallengeDetailNavKey("c_123"), navKey)
    }

    @Test
    fun parse_milestonesDeepLink_returnsCinephileWrappedNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/milestones")
        assertEquals(CinephileWrappedNavKey, navKey)
    }

    @Test
    fun parse_recommendationsDeepLink_returnsTasteProfileNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/recommendations")
        assertEquals(TasteProfileNavKey, navKey)
    }

    @Test
    fun parse_libraryHistoryDeepLink_returnsLibraryHomeNavKeyWithHistory() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/library/history")
        assertEquals(LibraryHomeNavKey(initialTab = LibraryTabDestination.History), navKey)
    }

    @Test
    fun parse_libraryCustomListsDeepLink_returnsLibraryHomeNavKeyWithCustomLists() {
        val navKey =
            ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/library/custom_lists")
        assertEquals(LibraryHomeNavKey(initialTab = LibraryTabDestination.CustomLists), navKey)
    }

    @Test
    fun parse_libraryCustomListDetailDeepLink_returnsLibraryHomeNavKeyWithTargetListId() {
        val navKey =
            ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/library/custom_lists/my_list_42")
        assertEquals(
            LibraryHomeNavKey(
                initialTab = LibraryTabDestination.CustomLists,
                targetCustomListId = "my_list_42"
            ),
            navKey
        )
    }

    @Test
    fun parse_matchRoomDeepLink_returnsMatchRoomNavKey() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/match/ROOM123")
        assertEquals(MatchRoomNavKey(roomCode = "ROOM123"), navKey)
    }

    @Test
    fun parse_movieMatchDeepLink_returnsMatchRoomNavKey() {
        val navKey =
            ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/movie/match/ROOM456")
        assertEquals(MatchRoomNavKey(roomCode = "ROOM456"), navKey)
    }

    @Test
    fun parse_swipenightDeepLink_returnsMatchRoomNavKey() {
        val navKey =
            ShowTimeDeepLinkHandler.parse("showtime://showtime.ssverma.in/swipenight/ROOM789")
        assertEquals(MatchRoomNavKey(roomCode = "ROOM789"), navKey)
    }
}

