package com.ssverma.core.navigation.nav3

import androidx.navigation3.runtime.NavKey

/**
 * Marker interface for [NavKey] instances that represent destinations that expand or reveal
 * from the top (e.g. search suggestions/typeahead, command palettes, docked bar expanders)
 * using Material 3 Expressive top-anchored vertical reveal and collapse motion.
 */
interface TopRevealNavKey : NavKey
