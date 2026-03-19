package com.ssverma.feature.filter.domain.processor

import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.SortBy

/**
 * A solid, future-proof wrapper for the extracted state.
 */
data class DiscoverFilterState(
    val options: List<DiscoverOption> = emptyList(),
    val sortBy: SortBy? = null,
) {
    /**
     * Safely combines two states together.
     * The incoming state overrides the sort/order if it exists.
     */
    fun merge(other: DiscoverFilterState): DiscoverFilterState {
        return DiscoverFilterState(
            options = this.options + other.options,
            sortBy = other.sortBy ?: this.sortBy,
        )
    }

    companion object {
        fun empty() = DiscoverFilterState()

        fun options(vararg options: DiscoverOption) = DiscoverFilterState(
            options = options.toList()
        )

        fun options(options: List<DiscoverOption>) = DiscoverFilterState(options = options)

        fun sort(sortBy: SortBy) = DiscoverFilterState(sortBy = sortBy)
    }
}
