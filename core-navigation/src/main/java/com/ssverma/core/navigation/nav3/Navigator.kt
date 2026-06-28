package com.ssverma.core.navigation.nav3

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            if (state.topLevelRoute == route) {
                // Re-clicking current tab: pop to root
                val currentStack = state.backStacks[route]
                while (currentStack != null && currentStack.size > 1) {
                    currentStack.removeLastOrNull()
                }
            } else {
                // Switch to new tab
                state.topLevelRoute = route
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
