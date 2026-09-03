package com.ssverma.showtime.navigation

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
    fun `parse discover deep link returns UniversalDiscoveryNavKey`() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/discover")
        assertEquals(UniversalDiscoveryNavKey(initialVibe = "ALL"), navKey)
    }

    @Test
    fun `parse discover vibe deep link returns UniversalDiscoveryNavKey with vibe`() {
        val navKey =
            ShowTimeDeepLinkHandler.parse("https://showtime.ssverma.in/discover/MIND_BENDING")
        assertEquals(UniversalDiscoveryNavKey(initialVibe = "MIND_BENDING"), navKey)
    }

    @Test
    fun parse_invalidHost_returnsNull() {
        val navKey = ShowTimeDeepLinkHandler.parse("https://www.google.com/tv/1399")
        assertNull(navKey)
    }
}

