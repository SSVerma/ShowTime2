package com.ssverma.shared.domain.model.feature

enum class CinephileFeature(
    val id: String,
    val introducedVersionCode: Int = 20000,
    val isBadgeEligible: Boolean = true
) {
    MY_LISTS("hub_my_lists", introducedVersionCode = 10012, isBadgeEligible = false),
    COMMUNITY_LISTS("hub_community_lists", introducedVersionCode = 20000, isBadgeEligible = true),
    CINEMA_DIARY("hub_cinema_diary", introducedVersionCode = 20000, isBadgeEligible = true),
    DAILY_GAME("hub_daily_game", introducedVersionCode = 20000, isBadgeEligible = true),
    DAILY_POLL("hub_daily_poll", introducedVersionCode = 20000, isBadgeEligible = true),
    MOVIE_MATCH("hub_movie_match", introducedVersionCode = 20000, isBadgeEligible = true),
    TASTE_PROFILE("hub_taste_profile", introducedVersionCode = 20000, isBadgeEligible = true),
    BACKLOG_CHALLENGES("hub_challenges", introducedVersionCode = 20000, isBadgeEligible = true),
    CINEMA_RECEIPT("hub_receipt", introducedVersionCode = 20000, isBadgeEligible = true),
    PEOPLE("hub_people", introducedVersionCode = 10012, isBadgeEligible = false),
    DISCOVERY("hub_discovery", introducedVersionCode = 20000, isBadgeEligible = true);

    fun isNew(acknowledgedFeatures: Set<String>): Boolean {
        return isBadgeEligible && id !in acknowledgedFeatures
    }
}
