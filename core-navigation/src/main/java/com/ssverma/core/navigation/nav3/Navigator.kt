package com.ssverma.core.navigation.nav3

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState) {
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
