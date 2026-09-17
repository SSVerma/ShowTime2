package com.ssverma.showtime.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.get
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.ssverma.core.navigation.nav3.LocalNavEntries
import com.ssverma.core.navigation.nav3.LocalNavigationState
import com.ssverma.core.navigation.nav3.LocalNavigator
import com.ssverma.core.navigation.nav3.LocalSharedTransitionScope
import com.ssverma.core.navigation.nav3.Nav3MetadataKeys
import com.ssverma.core.navigation.nav3.NavTransitionStyle
import com.ssverma.core.navigation.nav3.NavigationState
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.SequentialNavKey
import com.ssverma.core.navigation.nav3.TopRevealNavKey
import com.ssverma.core.navigation.nav3.toEntries

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ShowTimeNavDisplay(
    navigationState: NavigationState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
    openLibraryPage: (NavKey) -> Unit,
) {
    val entryProvider = rememberShowTimeEntryProvider(
        navigator = navigator,
        openLibraryPage = openLibraryPage
    )

    val entries = navigationState.toEntries(entryProvider)

    val topLevelOrder = remember {
        ShowTimeTopLevelNavItems.map { it.navKey }
    }

    val outerSharedScope = LocalSharedTransitionScope.current

    if (outerSharedScope != null) {
        CompositionLocalProvider(
            LocalNavigator provides navigator,
            LocalNavigationState provides navigationState,
            LocalNavEntries provides entries
        ) {
            NavDisplay(
                entries = entries,
                onBack = { navigator.goBack() },
                sharedTransitionScope = outerSharedScope,
                transitionSpec = { showTimeForwardTransition(topLevelOrder) },
                popTransitionSpec = { showTimePopTransition(topLevelOrder) },
                predictivePopTransitionSpec = { showTimePredictivePopTransition(topLevelOrder) },
                modifier = modifier.fillMaxSize()
            )
        }
    } else {
        SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
            CompositionLocalProvider(
                LocalNavigator provides navigator,
                LocalNavigationState provides navigationState,
                LocalNavEntries provides entries,
                LocalSharedTransitionScope provides this
            ) {
                NavDisplay(
                    entries = entries,
                    onBack = { navigator.goBack() },
                    sharedTransitionScope = this,
                    transitionSpec = { showTimeForwardTransition(topLevelOrder) },
                    popTransitionSpec = { showTimePopTransition(topLevelOrder) },
                    predictivePopTransitionSpec = { showTimePredictivePopTransition(topLevelOrder) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.showTimeForwardTransition(
    topLevelOrder: List<NavKey>
): ContentTransform {
    val initialNavKey = initialState.resolveNavKey()
    val targetNavKey = targetState.resolveNavKey()

    if (initialNavKey is SequentialNavKey && targetNavKey is SequentialNavKey) {
        if (initialNavKey.sequenceGroupId == targetNavKey.sequenceGroupId) {
            val isForward = targetNavKey.sequenceOrder > initialNavKey.sequenceOrder
            return createPeerSlideTransition(isForward = isForward)
        }
    }

    if (targetState.isTopReveal()) {
        return createTopRevealPushTransition()
    }

    val initialTabKey = initialState.entries.firstOrNull()?.metadata?.get(Nav3MetadataKeys.TabKey)
    val targetTabKey = targetState.entries.firstOrNull()?.metadata?.get(Nav3MetadataKeys.TabKey)
    val isTabSwitch = initialTabKey != targetTabKey

    return if (isTabSwitch) {
        createTabSwitchTransition(initialTabKey, targetTabKey, topLevelOrder)
    } else {
        createStackPushTransition()
    }
}

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.showTimePopTransition(
    topLevelOrder: List<NavKey>
): ContentTransform {
    val initialNavKey = initialState.resolveNavKey()
    val targetNavKey = targetState.resolveNavKey()

    if (initialNavKey is SequentialNavKey && targetNavKey is SequentialNavKey) {
        if (initialNavKey.sequenceGroupId == targetNavKey.sequenceGroupId) {
            val isForward = targetNavKey.sequenceOrder > initialNavKey.sequenceOrder
            return createPeerSlideTransition(isForward = isForward)
        }
    }

    if (initialState.isTopReveal()) {
        return createTopRevealPopTransition()
    }

    val initialTabKey = initialState.entries.firstOrNull()?.metadata?.get(Nav3MetadataKeys.TabKey)
    val targetTabKey = targetState.entries.firstOrNull()?.metadata?.get(Nav3MetadataKeys.TabKey)
    val isTabSwitch = initialTabKey != targetTabKey

    return if (isTabSwitch) {
        createTabSwitchTransition(initialTabKey, targetTabKey, topLevelOrder)
    } else {
        createStackPopTransition()
    }
}

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.showTimePredictivePopTransition(
    topLevelOrder: List<NavKey>
): ContentTransform {
    val initialNavKey = initialState.resolveNavKey()
    val targetNavKey = targetState.resolveNavKey()

    if (initialNavKey is SequentialNavKey && targetNavKey is SequentialNavKey) {
        if (initialNavKey.sequenceGroupId == targetNavKey.sequenceGroupId) {
            val isForward = targetNavKey.sequenceOrder > initialNavKey.sequenceOrder
            return createPeerSlideTransition(isForward = isForward)
        }
    }

    if (initialState.isTopReveal()) {
        return createTopRevealPopTransition()
    }

    val initialTabKey = initialState.entries.firstOrNull()?.metadata?.get(Nav3MetadataKeys.TabKey)
    val targetTabKey = targetState.entries.firstOrNull()?.metadata?.get(Nav3MetadataKeys.TabKey)
    val isTabSwitch = initialTabKey != targetTabKey

    return if (isTabSwitch) {
        createTabSwitchTransition(initialTabKey, targetTabKey, topLevelOrder)
    } else {
        (slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = { it / 5 }
        ) + fadeIn(
            initialAlpha = 0.85f
        ) + scaleIn(
            initialScale = 0.92f
        )).togetherWith(
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                targetOffset = { (it * 0.9f).toInt() }
            ) + scaleOut(
                targetScale = 0.90f
            )
        )
    }
}

private fun createTabSwitchTransition(
    initialTabKey: Any?,
    targetTabKey: Any?,
    topLevelOrder: List<NavKey>
): ContentTransform {
    val initialIdx = topLevelOrder.indexOf(initialTabKey as? NavKey)
    val targetIdx = topLevelOrder.indexOf(targetTabKey as? NavKey)
    val isForward = if (initialIdx != -1 && targetIdx != -1) targetIdx > initialIdx else true
    val directionMultiplier = if (isForward) 1 else -1

    return (slideInHorizontally(
        initialOffsetX = { (it * 0.12f * directionMultiplier).toInt() },
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(durationMillis = 240)
    )).togetherWith(
        slideOutHorizontally(
            targetOffsetX = { (-it * 0.12f * directionMultiplier).toInt() },
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 180)
        )
    )
}

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.createStackPushTransition(): ContentTransform {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
    ).togetherWith(
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = { it / 4 },
            animationSpec = tween(
                durationMillis = 320,
                easing = FastOutSlowInEasing
            )
        ) + fadeOut(
            targetAlpha = 0.85f,
            animationSpec = tween(durationMillis = 320)
        )
    )
}

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.createStackPopTransition(): ContentTransform {
    return (slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        initialOffset = { it / 4 },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
    ) + fadeIn(
        initialAlpha = 0.85f,
        animationSpec = tween(durationMillis = 280)
    )).togetherWith(
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(
                durationMillis = 280,
                easing = FastOutSlowInEasing
            )
        )
    )
}

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.createTopRevealPushTransition(): ContentTransform {
    return (expandVertically(
        expandFrom = Alignment.Top,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(durationMillis = 280, easing = LinearOutSlowInEasing)
    ) + scaleIn(
        initialScale = 0.94f,
        transformOrigin = TransformOrigin(0.5f, 0f),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
    )).togetherWith(
        fadeOut(
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
        ) + scaleOut(
            targetScale = 0.98f,
            transformOrigin = TransformOrigin(0.5f, 0f),
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
        )
    )
}

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.createTopRevealPopTransition(): ContentTransform {
    return (fadeIn(
        animationSpec = tween(
            durationMillis = 250,
            delayMillis = 50,
            easing = LinearOutSlowInEasing
        )
    ) + scaleIn(
        initialScale = 0.98f,
        transformOrigin = TransformOrigin(0.5f, 0f),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )).togetherWith(
        shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        ) + scaleOut(
            targetScale = 0.94f,
            transformOrigin = TransformOrigin(0.5f, 0f),
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
        )
    )
}

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.createPeerSlideTransition(
    isForward: Boolean
): ContentTransform {
    val slideDirection = if (isForward) {
        AnimatedContentTransitionScope.SlideDirection.Start
    } else {
        AnimatedContentTransitionScope.SlideDirection.End
    }

    return (slideIntoContainer(
        towards = slideDirection,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(durationMillis = 280),
        initialAlpha = 0.8f
    )).togetherWith(
        slideOutOfContainer(
            towards = slideDirection,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 220),
            targetAlpha = 0.8f
        )
    )
}

private fun <T : Any> Scene<T>.isTopReveal(): Boolean {
    val navKey = resolveNavKey()
    if (navKey is TopRevealNavKey || key is TopRevealNavKey) return true
    val style = entries.firstOrNull()?.metadata?.get(Nav3MetadataKeys.TransitionStyle)
    return style == NavTransitionStyle.TopReveal
}

private fun <T : Any> Scene<T>.resolveNavKey(): NavKey? {
    return entries.lastOrNull()?.let {
        (it.metadata[Nav3MetadataKeys.NavKey]
            ?: it.metadata[Nav3MetadataKeys.NavKey.toString()]) as? NavKey
    } ?: key as? NavKey
}
