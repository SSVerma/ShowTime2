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
        minVoteAverage = 0.0f,
        minVoteCount = 20
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
    NEON(93475, "NEON"),
    STUDIO_GHIBLI(10342, "Studio Ghibli"),
    PIXAR(3, "Pixar"),
    MARVEL(420, "Marvel Studios"),
    WARNER_BROS(174, "Warner Bros."),
    UNIVERSAL(33, "Universal Pictures"),
    PARAMOUNT(4, "Paramount"),
    COLUMBIA(5, "Columbia Pictures"),
    BLUMHOUSE(3172, "Blumhouse")
}

enum class DiscoveryTvNetworkHub(
    val networkId: Int,
    val label: String
) {
    HBO(49, "HBO"),
    NETFLIX(213, "Netflix"),
    APPLE_TV_PLUS(2552, "Apple TV+"),
    AMC(174, "AMC"),
    FX(88, "FX"),
    BBC(4, "BBC"),
    DISNEY_PLUS(2739, "Disney+"),
    SHOWTIME(67, "Showtime"),
    AMAZON(1024, "Prime Video"),
    PARAMOUNT_PLUS(4330, "Paramount+"),
    HULU(453, "Hulu")
}

enum class DiscoveryRatingThreshold(
    val minRating: Float,
    val label: String
) {
    ALL(0.0f, "Any Rating"),
    GOOD_6_PLUS(6.0f, "6.0+ Good"),
    GREAT_7_PLUS(7.0f, "7.0+ Great"),
    MASTERPIECE_8_PLUS(8.0f, "8.0+ Masterpiece")
}

enum class DiscoveryRuntimeRange(
    val minMinutes: Int?,
    val maxMinutes: Int?,
    val label: String
) {
    ALL(null, null, "Any Length"),
    SHORT(null, 90, "Under 90m"),
    STANDARD(90, 120, "90 – 120m"),
    EPIC(120, null, "Over 2 hours")
}

enum class DiscoveryMonetizationType(
    val apiValue: String,
    val label: String
) {
    STREAM("flatrate", "Stream"),
    FREE("free", "Free"),
    ADS("ads", "With Ads"),
    RENT("rent", "Rent"),
    BUY("buy", "Buy")
}

enum class DiscoveryLanguage(
    val isoCode: String?,
    val label: String
) {
    ALL(null, "All Languages"),
    ENGLISH("en", "English"),
    HINDI("hi", "Hindi"),
    KOREAN("ko", "Korean"),
    JAPANESE("ja", "Japanese"),
    SPANISH("es", "Spanish"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    ITALIAN("it", "Italian")
}

object DiscoveryCertificationHelper {
    fun getCertifications(regionCode: String, mediaType: MediaType): List<String> {
        return when (regionCode.uppercase()) {
            "IN" -> listOf("U", "U/A 7+", "U/A 13+", "U/A 16+", "A")
            "GB" -> listOf("U", "PG", "12A", "15", "18")
            "US" -> if (mediaType == MediaType.Movie) {
                listOf("G", "PG", "PG-13", "R", "NC-17")
            } else {
                listOf("TV-Y", "TV-G", "TV-PG", "TV-14", "TV-MA")
            }

            "CA" -> listOf("G", "PG", "14A", "18A", "R")
            "AU" -> listOf("G", "PG", "M", "MA15+", "R18+")
            "FR" -> listOf("U", "10", "12", "16", "18")
            "DE" -> listOf("0", "6", "12", "16", "18")
            else -> if (mediaType == MediaType.Movie) {
                listOf("G", "PG", "PG-13", "R", "NC-17")
            } else {
                listOf("TV-Y", "TV-PG", "TV-14", "TV-MA")
            }
        }
    }
}

data class UniversalDiscoveryFilter(
    val mediaType: MediaType = MediaType.Movie,
    val vibePreset: DiscoveryVibePreset = DiscoveryVibePreset.ALL,
    val decade: DiscoveryDecade = DiscoveryDecade.ALL_TIME,
    val sortOrder: DiscoverySortOrder = DiscoverySortOrder.POPULARITY_DESC,
    val studioHub: DiscoveryStudioHub? = null,
    val tvNetworkHub: DiscoveryTvNetworkHub? = null,
    val ratingThreshold: DiscoveryRatingThreshold = DiscoveryRatingThreshold.ALL,
    val runtimeRange: DiscoveryRuntimeRange = DiscoveryRuntimeRange.ALL,
    val monetizationTypes: Set<DiscoveryMonetizationType> = emptySet(),
    val language: DiscoveryLanguage = DiscoveryLanguage.ALL,
    val certification: String? = null,
    val selectedGenreIds: Set<Int> = emptySet(),
    val selectedProviderIds: Set<Int> = emptySet(),
    val watchRegion: String = "US",
    val minRating: Float? = null,
    val hideWatched: Boolean = true
) {
    fun activeFilterCount(): Int {
        var count = 0
        if (vibePreset != DiscoveryVibePreset.ALL) count++
        if (decade != DiscoveryDecade.ALL_TIME) count++
        if (sortOrder != DiscoverySortOrder.POPULARITY_DESC) count++
        if (mediaType == MediaType.Movie && studioHub != null) count++
        if (mediaType == MediaType.Tv && tvNetworkHub != null) count++
        if (ratingThreshold != DiscoveryRatingThreshold.ALL || (minRating != null && minRating > 0f)) count++
        if (mediaType == MediaType.Movie && runtimeRange != DiscoveryRuntimeRange.ALL) count++
        if (monetizationTypes.isNotEmpty()) count += monetizationTypes.size
        if (language != DiscoveryLanguage.ALL) count++
        if (!certification.isNullOrBlank()) count++
        if (selectedGenreIds.isNotEmpty()) count += selectedGenreIds.size
        if (selectedProviderIds.isNotEmpty()) count += selectedProviderIds.size
        if (!hideWatched) count++
        return count
    }
}

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
    val displayYear: String = "",
    val genreNames: List<String> = emptyList(),
    val isWatched: Boolean = false,
    val isFavorite: Boolean = false,
    val isInWatchlist: Boolean = false,
    val isUpcoming: Boolean = false
)
