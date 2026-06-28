package com.ssverma.core.navigation.nav3

import androidx.compose.runtime.staticCompositionLocalOf

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("No Navigator provided")
}

val LocalNavigationState = staticCompositionLocalOf<NavigationState> {
    error("No NavigationState provided")
}

val LocalNavEntries = staticCompositionLocalOf<List<NavEntry<NavKey>>> {
    emptyList()
}
