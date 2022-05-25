package com.ssverma.core.navigation

import androidx.navigation.NamedNavArgument

interface Destination {
    val placeholderRoute: PlaceholderRoute
    val arguments: List<NamedNavArgument>
}

abstract class StandaloneDestination(private val identifier: String) : Destination {
    override val placeholderRoute: PlaceholderRoute
        get() = Route.placeholderOf(identifier).build()

    override val arguments: List<NamedNavArgument>
        get() = placeholderRoute.navArguments

    val actualRoute: ActualRoute
        get() = Route.actualOf(identifier).build()
}

abstract class DependentDestination<T>(private val identifier: String) : Destination {
    override val placeholderRoute: PlaceholderRoute
        get() = placeholderRoute(Route.placeholderOf(identifier))

    override val arguments: List<NamedNavArgument>
        get() = placeholderRoute.navArguments

    abstract fun placeholderRoute(
        builder: PlaceholderRoute.PlaceHolderRouteBuilder
    ): PlaceholderRoute

    abstract fun actualRoute(
        input: T,
        builder: ActualRoute.ActualRouteBuilder = Route.actualOf(identifier)
    ): ActualRoute
}

abstract class GraphDestination(identifier: String) : StandaloneDestination(identifier)