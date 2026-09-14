package com.ssverma.core.navigation.nav3

import androidx.navigation3.runtime.NavKey

/**
 * Marker interface for [NavKey] instances that represent ordered items in a sequence or pager
 * (e.g., episodes within a season, pages in a document, steps in an onboarding flow).
 *
 * When navigating between two keys that implement [SequentialNavKey] and share the same
 * [sequenceGroupId], the navigation framework performs a direction-aware peer slide transition
 * (forward or backward) rather than a standard hierarchical push/pop.
 */
interface SequentialNavKey : NavKey {
    /**
     * Unique identifier for the sequence group (e.g. show ID + season number).
     * Keys with equal [sequenceGroupId] are considered peers in the same sequence.
     */
    val sequenceGroupId: Any

    /**
     * Position or order within the sequence (e.g. episode number).
     */
    val sequenceOrder: Int
}
