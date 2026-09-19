package com.ssverma.shared.domain.model.community

enum class MediaReactionTag(
    val tagKey: String,
    val isPreReleaseTag: Boolean = false
) {
    // Post-Release Audience Vibes
    MIND_BENDING(tagKey = "mind_bending", isPreReleaseTag = false),
    COMFORT_WATCH(tagKey = "comfort_watch", isPreReleaseTag = false),
    PLOT_TWIST(tagKey = "plot_twist", isPreReleaseTag = false),
    IMAX_ESSENTIAL(tagKey = "imax_essential", isPreReleaseTag = false),
    EMOTIONAL_TEARJERKER(tagKey = "cried_eyes_out", isPreReleaseTag = false),
    OVERRATED(tagKey = "overrated", isPreReleaseTag = false),

    // Pre-Release Anticipation & Hype Vibes
    HYPED(tagKey = "hyped", isPreReleaseTag = true),
    DAY_ONE_CINEMA(tagKey = "day_one_cinema", isPreReleaseTag = true),
    SKEPTICAL(tagKey = "skeptical", isPreReleaseTag = true),
    MOST_ANTICIPATED(tagKey = "most_anticipated", isPreReleaseTag = true),
    TRAILER_HOOKED(tagKey = "trailer_hooked", isPreReleaseTag = true);

    companion object {
        fun fromTagKey(key: String): MediaReactionTag? {
            return entries.find { it.tagKey == key }
        }

        fun preReleaseTags(): List<MediaReactionTag> {
            return entries.filter { it.isPreReleaseTag }
        }

        fun postReleaseTags(): List<MediaReactionTag> {
            return entries.filterNot { it.isPreReleaseTag }
        }

        fun tagsFor(isUpcoming: Boolean): List<MediaReactionTag> {
            return if (isUpcoming) preReleaseTags() else postReleaseTags()
        }
    }
}
