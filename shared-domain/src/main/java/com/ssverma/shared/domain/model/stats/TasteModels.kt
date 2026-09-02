package com.ssverma.shared.domain.model.stats

import com.ssverma.shared.domain.model.discovery.UniversalMediaItem

data class TasteGenreDistribution(
    val genreName: String,
    val count: Int,
    val percentage: Float
)

data class TasteEraDistribution(
    val eraLabel: String,
    val count: Int,
    val percentage: Float
)

data class TasteRatingDistribution(
    val ratingBand: String,
    val starRating: Float,
    val count: Int,
    val percentage: Float
)

enum class CinephilePersona(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val description: String
) {
    ACTION_BUFF(
        title = "Action & Adventure Aficionado",
        subtitle = "Lover of adrenaline and heroic journeys",
        emoji = "💥",
        description = "You thrive on high-octane thrills, kinetic set pieces, and epic blockbuster storytelling."
    ),
    SCI_FI_EXPLORER(
        title = "Sci-Fi & Speculative Visionary",
        subtitle = "Explorer of strange worlds and future horizons",
        emoji = "🌌",
        description = "You gravitate towards mind-bending concepts, cosmic mysteries, and world-building depth."
    ),
    DRAMA_DEVOTEE(
        title = "Prestige Drama Connoisseur",
        subtitle = "Appreciator of profound character studies",
        emoji = "🎭",
        description = "You appreciate intimate performances, thematic nuance, and gripping emotional stakes."
    ),
    COMEDY_CHAMPION(
        title = "Feel-Good & Comedy Seeker",
        subtitle = "Seeker of wit, satire, and good vibes",
        emoji = "🍿",
        description = "You love sharp comedic timing, heartwarming arcs, and engaging entertainment."
    ),
    THRILLER_DETECTIVE(
        title = "Mystery & Suspense Sleuth",
        subtitle = "Master of plot twists and psychological intrigue",
        emoji = "🔍",
        description = "You love unraveling intricate puzzles, dark secrets, and high-tension pacing."
    ),
    ANIMATION_FANATIC(
        title = "Animation & Visual Artistry Buff",
        subtitle = "Celebrator of imaginative illustration and anime",
        emoji = "🎨",
        description = "You champion the boundless creative possibilities of animated cinema and television."
    ),
    ECLECTIC_CINEPHILE(
        title = "Eclectic Cinephile",
        subtitle = "Curator of multifaceted cinematic horizons",
        emoji = "✨",
        description = "Your tastes span broad genres and eras, always curious for remarkable stories."
    )
}

data class TasteProfileStats(
    val totalWatchedMinutes: Int = 0,
    val totalWatchedHours: Int = 0,
    val totalWatchedDays: Float = 0f,
    val totalItemsLogged: Int = 0,
    val totalMoviesLogged: Int = 0,
    val totalTvLogged: Int = 0,
    val averageRating: Float = 0f,
    val rewatchCount: Int = 0,
    val rewatchPercentage: Float = 0f,
    val persona: CinephilePersona = CinephilePersona.ECLECTIC_CINEPHILE,
    val topGenres: List<TasteGenreDistribution> = emptyList(),
    val ratingDistribution: List<TasteRatingDistribution> = emptyList(),
    val eraDistribution: List<TasteEraDistribution> = emptyList(),
    val topRatedSeedTitles: List<String> = emptyList()
)

data class RecommendationShelf(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val badge: String? = null,
    val emoji: String = "✨",
    val items: List<UniversalMediaItem> = emptyList()
)
