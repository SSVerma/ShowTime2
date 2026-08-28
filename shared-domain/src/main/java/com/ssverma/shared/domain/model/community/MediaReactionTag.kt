package com.ssverma.shared.domain.model.community

enum class MediaReactionTag(val tagKey: String) {
    MIND_BENDING("mind_bending"),
    COMFORT_WATCH("comfort_watch"),
    PLOT_TWIST("plot_twist"),
    IMAX_ESSENTIAL("imax_essential"),
    EMOTIONAL_TEARJERKER("cried_eyes_out"),
    OVERRATED("overrated");

    companion object {
        fun fromTagKey(key: String): MediaReactionTag? {
            return entries.find { it.tagKey == key }
        }
    }
}
