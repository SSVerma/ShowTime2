package com.ssverma.shared.domain.model.feature

enum class CinephileFeature(
    val id: String,
    val isBadgeEligible: Boolean = true
) {
    MY_LISTS("hub_my_lists", isBadgeEligible = false),
    COMMUNITY_LISTS("hub_community_lists", isBadgeEligible = true),
    CINEMA_DIARY("hub_cinema_diary", isBadgeEligible = true),
    DAILY_GAME("hub_daily_game", isBadgeEligible = true),
    DAILY_POLL("hub_daily_poll", isBadgeEligible = true),
    MOVIE_MATCH("hub_movie_match", isBadgeEligible = true),
    TASTE_PROFILE("hub_taste_profile", isBadgeEligible = true),
    BACKLOG_CHALLENGES("hub_challenges", isBadgeEligible = true),
    CINEMA_RECEIPT("hub_receipt", isBadgeEligible = true),
    PEOPLE("hub_people", isBadgeEligible = false),
    DISCOVERY("hub_discovery", isBadgeEligible = true),
    HOME_SCREEN_WIDGETS("hub_widgets", isBadgeEligible = true);

    fun isNew(acknowledgedFeatures: Set<String>): Boolean {
        return isBadgeEligible && id !in acknowledgedFeatures
    }
}
