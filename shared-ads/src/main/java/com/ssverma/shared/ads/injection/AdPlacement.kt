package com.ssverma.shared.ads.injection

sealed class AdPlacement {
    /**
     * Inserts ads at specific absolute indices in the list.
     */
    data class Fixed(val positions: List<Int>) : AdPlacement()

    /**
     * Inserts ads starting from [start] and then every [frequency] items.
     */
    data class Repeating(val start: Int, val frequency: Int) : AdPlacement()

    object None : AdPlacement()
}
