package com.ssverma.shared.domain.model.discovery

import com.ssverma.shared.domain.model.MediaType

enum class DiscoveryVibePreset(
    val label: String,
    val emoji: String,
    val movieGenreIds: List<Int> = emptyList(),
    val tvGenreIds: List<Int> = emptyList(),
    val minVoteAverage: Float = 7.0f,
    val minVoteCount: Int = 100,
    val maxRuntimeMinutes: Int? = null
) {
    ALL(
        label = "All Vibes",
        emoji = "✨",
        minVoteAverage = 6.5f,
        minVoteCount = 100
    ),
    MIND_BENDING(
        label = "Mind-Bending",
        emoji = "🤯",
        movieGenreIds = listOf(878, 9648, 53), // Sci-Fi, Mystery, Thriller
        tvGenreIds = listOf(10765, 9648, 80),  // Sci-Fi & Fantasy, Mystery, Crime
        minVoteAverage = 7.2f,
        minVoteCount = 150
    ),
    PURE_FUN(
        label = "Pure Fun",
        emoji = "🍿",
        movieGenreIds = listOf(28, 35, 12),    // Action, Comedy, Adventure
        tvGenreIds = listOf(10759, 35),        // Action & Adventure, Comedy
        minVoteAverage = 6.8f,
        minVoteCount = 100
    ),
    DARK_AND_GRITTY(
        label = "Dark & Gritty",
        emoji = "🔪",
        movieGenreIds = listOf(80, 53, 18),    // Crime, Thriller, Drama
        tvGenreIds = listOf(80, 18),           // Crime, Drama
        minVoteAverage = 7.0f,
        minVoteCount = 120
    ),
    COMFORT_BINGE(
        label = "Comfort Binge",
        emoji = "🛋️",
        movieGenreIds = listOf(16, 35, 10751), // Animation, Comedy, Family
        tvGenreIds = listOf(16, 35, 10751),    // Animation, Comedy, Family
        minVoteAverage = 7.0f,
        minVoteCount = 100
    ),
    EPIC_WORLDS(
        label = "Epic Worlds",
        emoji = "🌌",
        movieGenreIds = listOf(14, 878, 12),   // Fantasy, Sci-Fi, Adventure
        tvGenreIds = listOf(10765, 10759),     // Sci-Fi & Fantasy, Action & Adventure
        minVoteAverage = 7.2f,
        minVoteCount = 150
    ),
    LATE_NIGHT_CHILLS(
        label = "Late Night Chills",
        emoji = "😱",
        movieGenreIds = listOf(27, 9648),      // Horror, Mystery
        tvGenreIds = listOf(9648),             // Mystery
        minVoteAverage = 6.5f,
        minVoteCount = 100
    ),
    MASTERPIECES(
        label = "Masterpieces",
        emoji = "🏆",
        minVoteAverage = 8.0f,
        minVoteCount = 300
    ),
    QUICK_WATCH(
        label = "Quick Watch",
        emoji = "⏳",
        minVoteAverage = 6.8f,
        minVoteCount = 100,
        maxRuntimeMinutes = 100
    )
}

enum class DiscoveryDecade(
    val label: String,
    val startYear: Int?,
    val endYear: Int?
) {
    ALL_TIME("All Time", null, null),
    TWENTIES_2020S("2020s", 2020, 2029),
    TENS_2010S("2010s", 2010, 2019),
    AUGHTS_2000S("2000s", 2000, 2009),
    NINETIES_1990S("90s Classics", 1990, 1999),
    EIGHTIES_1980S("80s Neon", 1980, 1989),
    SEVENTIES_1970S("70s Cinema", 1970, 1979),
    GOLDEN_AGE("Golden Age", 1920, 1969)
}

enum class DiscoverySortOrder(
    val label: String,
    val apiValue: String
) {
    POPULARITY_DESC("Most Popular", "popularity.desc"),
    VOTE_AVERAGE_DESC("Highest Rated", "vote_average.desc"),
    RELEASE_DATE_DESC("Release Date (Newest)", "primary_release_date.desc"),
    VOTE_COUNT_DESC("Most Reviewed", "vote_count.desc")
}

enum class DiscoveryStudioHub(
    val companyId: Int,
    val label: String
) {
    A24(41077, "A24"),
    HBO(3268, "HBO"),
    NEON(93475, "NEON"),
    STUDIO_GHIBLI(10342, "Studio Ghibli"),
    PIXAR(3, "Pixar"),
    MARVEL(420, "Marvel Studios"),
    WARNER_BROS(174, "Warner Bros.")
}

data class UniversalDiscoveryFilter(
    val mediaType: MediaType = MediaType.Movie,
    val vibePreset: DiscoveryVibePreset = DiscoveryVibePreset.ALL,
    val decade: DiscoveryDecade = DiscoveryDecade.ALL_TIME,
    val sortOrder: DiscoverySortOrder = DiscoverySortOrder.POPULARITY_DESC,
    val studioHub: DiscoveryStudioHub? = null,
    val selectedGenreIds: Set<Int> = emptySet(),
    val selectedProviderIds: Set<Int> = emptySet(),
    val watchRegion: String = "US",
    val minRating: Float? = null,
    val hideWatched: Boolean = true
)

data class UniversalMediaItem(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val overview: String,
    val posterImageUrl: String,
    val backdropImageUrl: String,
    val voteAvg: Float,
    val voteCount: Int,
    val releaseDate: String,
    val genreNames: List<String> = emptyList(),
    val isWatched: Boolean = false,
    val isFavorite: Boolean = false,
    val isInWatchlist: Boolean = false
)
