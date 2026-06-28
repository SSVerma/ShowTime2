package com.ssverma.showtime.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.get
import androidx.navigation3.ui.NavDisplay
import com.ssverma.core.navigation.nav3.LocalNavEntries
import com.ssverma.core.navigation.nav3.LocalNavigationState
import com.ssverma.core.navigation.nav3.LocalNavigator
import com.ssverma.core.navigation.nav3.Nav3MetadataKeys
import com.ssverma.core.navigation.nav3.NavigationState
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.toEntries

@Composable
fun ShowTimeNavDisplay(
    navigationState: NavigationState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
    openLibraryPage: () -> Unit,
) {
    val entryProvider = rememberShowTimeEntryProvider(
        navigator = navigator,
        openLibraryPage = openLibraryPage
    )

    val entries = navigationState.toEntries(entryProvider)

    val tabStiffness = 500f // Balanced, snappy but smooth for tab switches
    val stackStiffness = 800f // Snappy and polished for stack navigation

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalNavigationState provides navigationState,
        LocalNavEntries provides entries
    ) {
        NavDisplay(
            entries = entries,
            onBack = { navigator.goBack() },
            transitionSpec = {
                val initialTabKey =
                    initialState.entries.firstOrNull()?.metadata?.get(Nav3MetadataKeys.TabKey)
                val targetTabKey =
                    targetState.entries.firstOrNull()?.metadata?.get(Nav3MetadataKeys.TabKey)
                val isTabSwitch = initialTabKey != targetTabKey

                if (isTabSwitch) {
                    // Soft vertical slide for tab switches
                    (slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            stiffness = tabStiffness,
                            dampingRatio = Spring.DampingRatioLowBouncy
                        )
                    ) + fadeIn(animationSpec = spring(stiffness = tabStiffness))).togetherWith(
                        slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = spring(stiffness = tabStiffness)
                        ) + fadeOut(animationSpec = spring(stiffness = tabStiffness))
                    )
                } else {
                    // Solid horizontal parallax for stack navigation
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = spring(stiffness = stackStiffness)
                    ) togetherWith slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        targetOffset = { it / 4 },
                        animationSpec = spring(stiffness = stackStiffness)
                    )
                }
            },
            popTransitionSpec = {
                val initialTabKey =
                    initialState.entries.firstOrNull()?.metadata?.get(Nav3MetadataKeys.TabKey)
                val targetTabKey =
                    targetState.entries.firstOrNull()?.metadata?.get(Nav3MetadataKeys.TabKey)
                val isTabSwitch = initialTabKey != targetTabKey

                if (isTabSwitch) {
                    // Soft vertical slide for tab switches
                    (slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = spring(
                            stiffness = tabStiffness,
                            dampingRatio = Spring.DampingRatioLowBouncy
                        )
                    ) + fadeIn(animationSpec = spring(stiffness = tabStiffness))).togetherWith(
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = spring(stiffness = tabStiffness)
                        ) + fadeOut(animationSpec = spring(stiffness = tabStiffness))
                    )
                } else {
                    // Solid horizontal parallax for stack navigation
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        initialOffset = { it / 4 },
                        animationSpec = spring(stiffness = stackStiffness)
                    ) togetherWith slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = spring(stiffness = stackStiffness)
                    )
                }
            },
            predictivePopTransitionSpec = {
                // Predictive back remains horizontal
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    initialOffset = { it / 4 },
                    animationSpec = spring(stiffness = stackStiffness)
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = spring(stiffness = stackStiffness)
                )
            },
            modifier = modifier.fillMaxSize()
        )
    }
}
