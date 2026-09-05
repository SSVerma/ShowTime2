package com.ssverma.core.navigation.nav3

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.staticCompositionLocalOf

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey

val LocalNavigator = staticCompositionLocalOf<Navigator?> {
    null
}

val LocalNavigationState = staticCompositionLocalOf<NavigationState> {
    error("No NavigationState provided")
}

val LocalNavEntries = staticCompositionLocalOf<List<NavEntry<NavKey>>> {
    emptyList()
}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> {
    null
}

val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> {
    null
}
