package com.ssverma.core.navigation.nav3

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState) {
    var isMultiPaneActive: Boolean = false

    fun navigate(route: NavKey) {
        val matchingTopLevel =
            state.backStacks.keys.firstOrNull { it == route || it::class == route::class }
        if (matchingTopLevel != null) {
            if (state.topLevelRoute == matchingTopLevel) {
                val currentStack = state.backStacks[matchingTopLevel]
                if (currentStack != null) {
                    if (route != matchingTopLevel) {
                        currentStack.clear()
                        currentStack.add(route)
                    } else {
                        while (currentStack.size > 1) {
                            currentStack.removeLastOrNull()
                        }
                    }
                }
            } else {
                val targetStack = state.backStacks[matchingTopLevel]
                if (targetStack != null && route != matchingTopLevel) {
                    targetStack.clear()
                    targetStack.add(route)
                }
                state.topLevelRoute = matchingTopLevel
            }
        } else {
            // Push onto current stack
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: return

        if (currentStack.size > 1) {
            // Pop current stack
            currentStack.removeLastOrNull()
        } else {
            // Current stack is at base, switch back to start route if we're not already there
            if (state.topLevelRoute != state.startRoute) {
                state.topLevelRoute = state.startRoute
            }
        }
    }

    /**
     * Navigates from a list pane to a detail destination. When an adjacent multi-pane detail pane
     * is already visible on wide displays, replaces the trailing detail/extra chain in-place so
     * clicking consecutive list items updates the detail pane cleanly without stacking entries.
     */
    fun navigateFromListToDetail(route: NavKey) {
        val currentStack = state.backStacks[state.topLevelRoute]
        if (isMultiPaneActive &&
            currentStack != null &&
            currentStack.size >= 2 &&
            (currentStack.last() is AdaptiveDetailNavKey || currentStack.last() is AdaptiveExtraNavKey)
        ) {
            while (
                currentStack.size >= 2 &&
                (currentStack.last() is AdaptiveDetailNavKey || currentStack.last() is AdaptiveExtraNavKey)
            ) {
                currentStack.removeLastOrNull()
            }
            currentStack.add(route)
        } else {
            navigate(route)
        }
    }

    /**
     * Returns true when [detailKey] is the primary side-by-side detail pane rendered right beside
     * a list pane on wide viewports, so its header button can present a close affordance rather
     * than a redundant back arrow next to the list pane's back button.
     */
    fun isSideBySideDetailRoot(detailKey: NavKey): Boolean {
        if (!isMultiPaneActive) return false
        val currentStack = state.backStacks[state.topLevelRoute] ?: return false
        if (currentStack.size < 2 || currentStack.lastOrNull() != detailKey) return false
        val previousKey = currentStack[currentStack.lastIndex - 1]
        return previousKey !is AdaptiveDetailNavKey && previousKey !is AdaptiveExtraNavKey
    }

    /**
     * Navigates up from a list pane's own toolbar back button. When an adjacent detail or extra
     * chain is open alongside the list pane on wide viewports, clears that trailing chain first so
     * pressing back on the list pane itself exits cleanly in a single tap.
     */
    fun navigateUpFromListPane() {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return
        if (isMultiPaneActive) {
            while (
                currentStack.size >= 2 &&
                (currentStack.last() is AdaptiveDetailNavKey || currentStack.last() is AdaptiveExtraNavKey)
            ) {
                currentStack.removeLastOrNull()
            }
        }
        goBack()
    }

    /**
     * Replaces the current top destination with a new one.
     * Useful for in-place episode navigation (prev/next) where the backstack should not grow.
     */
    fun navigateReplace(route: NavKey) {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return
        if (currentStack.isNotEmpty()) {
            currentStack.removeLastOrNull()
        }
        currentStack.add(route)
    }

    /**
     * Checks if the destination immediately below the current destination in the stack matches [predicate].
     */
    fun isPreviousDestination(predicate: (NavKey) -> Boolean): Boolean {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return false
        if (currentStack.size < 2) return false
        return predicate(currentStack[currentStack.size - 2])
    }

    /**
     * Pops to previous destination if it matches [predicate], otherwise executes [onFallback].
     */
    fun popOr(predicate: (NavKey) -> Boolean, onFallback: () -> Unit) {
        if (isPreviousDestination(predicate)) {
            goBack()
        } else {
            onFallback()
        }
    }

    /**
     * Pops the current destination and sets a result for the previous one.
     */
    fun goBack(result: Any) {
        setResult(result)
        goBack()
    }

    /**
     * Stores a result for the destination that is currently below the top destination in the stack.
     */
    fun setResult(result: Any) {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return
        if (currentStack.size < 2) return

        // We use the string representation of the key as the identifier for the result.
        // This matches contentKey logic.
        val targetKey = currentStack[currentStack.size - 2]
        state.setResult(targetKey.toString(), result)
    }

    /**
     * Consumes (reads and clears) a result intended for the current destination.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> consumeResult(): T? {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return null
        val currentKey = currentStack.lastOrNull() ?: return null
        return state.consumeResult(currentKey.toString()) as? T
    }
}
