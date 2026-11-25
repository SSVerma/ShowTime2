package com.ssverma.core.navigation.nav3

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NavigatorAdaptiveDetailTest {

    private data object HomeRootKey : NavKey
    private data object SearchListKey : NavKey
    private data class SampleDetailKey(val itemId: Int) : AdaptiveDetailNavKey
    private data class SampleExtraKey(val itemId: Int) : AdaptiveExtraNavKey

    private fun createNavigator(isMultiPaneActive: Boolean): Pair<Navigator, NavBackStack<NavKey>> {
        val homeBackStack = NavBackStack<NavKey>(HomeRootKey)
        val navigationState = NavigationState(
            startRoute = HomeRootKey,
            topLevelRoute = mutableStateOf(HomeRootKey),
            backStacks = mapOf(HomeRootKey to homeBackStack),
            resultsState = mutableStateOf(emptyMap())
        )
        val navigator = Navigator(state = navigationState).apply {
            this.isMultiPaneActive = isMultiPaneActive
        }
        return navigator to homeBackStack
    }

    @Test
    fun singlePaneMode_navigateFromListToDetail_pushesConsecutiveDetails() {
        val (navigator, backStack) = createNavigator(isMultiPaneActive = false)

        navigator.navigate(SearchListKey)
        navigator.navigateFromListToDetail(SampleDetailKey(itemId = 1))
        navigator.navigateFromListToDetail(SampleDetailKey(itemId = 2))

        assertThat(backStack.toList()).containsExactly(
            HomeRootKey,
            SearchListKey,
            SampleDetailKey(itemId = 1),
            SampleDetailKey(itemId = 2)
        ).inOrder()
    }

    @Test
    fun multiPaneMode_navigateFromListToDetail_fromBareList_pushesDetail() {
        val (navigator, backStack) = createNavigator(isMultiPaneActive = true)

        navigator.navigate(SearchListKey)
        navigator.navigateFromListToDetail(SampleDetailKey(itemId = 10))

        assertThat(backStack.toList()).containsExactly(
            HomeRootKey,
            SearchListKey,
            SampleDetailKey(itemId = 10)
        ).inOrder()
    }

    @Test
    fun multiPaneMode_navigateFromListToDetail_replacesActiveDetailInPlace() {
        val (navigator, backStack) = createNavigator(isMultiPaneActive = true)

        navigator.navigate(SearchListKey)
        navigator.navigateFromListToDetail(SampleDetailKey(itemId = 10))
        navigator.navigateFromListToDetail(SampleDetailKey(itemId = 20))
        navigator.navigateFromListToDetail(SampleDetailKey(itemId = 30))

        assertThat(backStack.toList()).containsExactly(
            HomeRootKey,
            SearchListKey,
            SampleDetailKey(itemId = 30)
        ).inOrder()
    }

    @Test
    fun multiPaneMode_navigateFromListToDetail_clearsNestedDetailAndExtraChainInPlace() {
        val (navigator, backStack) = createNavigator(isMultiPaneActive = true)

        navigator.navigate(SearchListKey)
        navigator.navigateFromListToDetail(SampleDetailKey(itemId = 10))
        navigator.navigate(SampleDetailKey(itemId = 11))
        navigator.navigate(SampleExtraKey(itemId = 11))

        navigator.navigateFromListToDetail(SampleDetailKey(itemId = 99))

        assertThat(backStack.toList()).containsExactly(
            HomeRootKey,
            SearchListKey,
            SampleDetailKey(itemId = 99)
        ).inOrder()
    }

    @Test
    fun multiPaneMode_nestedNavigate_pushesOntoStackAndGoBackPopsCleanly() {
        val (navigator, backStack) = createNavigator(isMultiPaneActive = true)

        navigator.navigate(SearchListKey)
        navigator.navigateFromListToDetail(SampleDetailKey(itemId = 10))
        navigator.navigate(SampleDetailKey(itemId = 42))

        assertThat(backStack.toList()).containsExactly(
            HomeRootKey,
            SearchListKey,
            SampleDetailKey(itemId = 10),
            SampleDetailKey(itemId = 42)
        ).inOrder()

        navigator.goBack()

        assertThat(backStack.toList()).containsExactly(
            HomeRootKey,
            SearchListKey,
            SampleDetailKey(itemId = 10)
        ).inOrder()
    }

    @Test
    fun isSideBySideDetailRoot_distinguishesRootDetailFromNestedDetailAndSinglePane() {
        val (multiPaneNavigator, _) = createNavigator(isMultiPaneActive = true)
        val rootDetail = SampleDetailKey(itemId = 1)
        val nestedDetail = SampleDetailKey(itemId = 2)

        multiPaneNavigator.navigate(SearchListKey)
        multiPaneNavigator.navigateFromListToDetail(rootDetail)
        assertThat(multiPaneNavigator.isSideBySideDetailRoot(rootDetail)).isTrue()

        multiPaneNavigator.navigate(nestedDetail)
        assertThat(multiPaneNavigator.isSideBySideDetailRoot(rootDetail)).isFalse()
        assertThat(multiPaneNavigator.isSideBySideDetailRoot(nestedDetail)).isFalse()

        val (singlePaneNavigator, _) = createNavigator(isMultiPaneActive = false)
        singlePaneNavigator.navigate(SearchListKey)
        singlePaneNavigator.navigateFromListToDetail(rootDetail)
        assertThat(singlePaneNavigator.isSideBySideDetailRoot(rootDetail)).isFalse()
    }

    @Test
    fun navigateUpFromListPane_clearsTrailingDetailAndExtraThenExitsList() {
        val (navigator, backStack) = createNavigator(isMultiPaneActive = true)

        navigator.navigate(SearchListKey)
        navigator.navigateFromListToDetail(SampleDetailKey(itemId = 1))
        navigator.navigate(SampleExtraKey(itemId = 1))

        navigator.goBack()
        assertThat(backStack.toList()).containsExactly(
            HomeRootKey,
            SearchListKey,
            SampleDetailKey(itemId = 1)
        ).inOrder()

        navigator.navigate(SampleExtraKey(itemId = 1))
        navigator.navigateUpFromListPane()
        assertThat(backStack.toList()).containsExactly(HomeRootKey).inOrder()
    }
}
