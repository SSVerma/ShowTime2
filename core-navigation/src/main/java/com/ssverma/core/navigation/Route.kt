package com.ssverma.core.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

abstract class Route(
    private val builtRoute: String
) {

    companion object {
        fun placeholderOf(identifier: String): PlaceholderRoute.PlaceHolderRouteBuilder {
            return PlaceholderRoute.PlaceHolderRouteBuilder(identifier)
        }

        fun actualOf(identifier: String): ActualRoute.ActualRouteBuilder {
            return ActualRoute.ActualRouteBuilder(identifier)
        }
    }

    fun asNavRoute(): String {
        return builtRoute
    }
}

class PlaceholderRoute private constructor(
    route: String,
    val navArguments: List<NamedNavArgument>
) : Route(route) {

    class PlaceHolderRouteBuilder internal constructor(
        private val route: String
    ) : RouteBuilder<PlaceholderRoute>() {

        private val navArguments = mutableListOf<NamedNavArgument>()

        fun mandatoryArg(
            name: String,
            navType: NavType<*>
        ): PlaceHolderRouteBuilder {
            this.navArguments += navArgument(name) {
                type = navType
            }
            return super.mandatoryArg(name, name) as PlaceHolderRouteBuilder
        }

        fun optionalArg(
            name: String,
            navType: NavType<*>,
            isNullable: Boolean = false,
            defaultVal: Any? = null
        ): PlaceHolderRouteBuilder {
            this.navArguments += navArgument(name) {
                type = navType
                nullable = isNullable
                defaultValue = defaultVal
            }
            return super.optionalArg(name, name) as PlaceHolderRouteBuilder
        }

        override fun generateNavRoute(): String {
            var completeRoute = mandatoryArgs.keys.joinToString(
                separator = "/",
                transform = { "{$it}" },
                prefix = if (mandatoryArgs.isEmpty()) {
                    route
                } else {
                    "$route/"
                },
            )

            optionalArgs.keys.forEachIndexed { index, arg ->
                completeRoute = if (index == 0) {
                    "$completeRoute?$arg={$arg}"
                } else {
                    "$completeRoute&$arg={$arg}"
                }
            }

            return completeRoute
        }

        override fun build(): PlaceholderRoute {
            return PlaceholderRoute(route = generateNavRoute(), navArguments = navArguments)
        }
    }
}

class ActualRoute private constructor(
    route: String
) : Route(route) {

    class ActualRouteBuilder internal constructor(
        private val route: String
    ) : RouteBuilder<ActualRoute>() {

        override fun generateNavRoute(): String {
            var completeRoute = mandatoryArgs.keys.joinToString(
                separator = "/",
                transform = { mandatoryArgs[it]!! },
                prefix = if (mandatoryArgs.isEmpty()) {
                    route
                } else {
                    "$route/"
                },
            )

            optionalArgs.keys.forEachIndexed { index, key ->
                optionalArgs[key]?.let { value ->
                    completeRoute = if (index == 0) {
                        "$completeRoute?$key=$value"
                    } else {
                        "$completeRoute&$key=$value"
                    }
                }
            }

            return completeRoute
        }

        override fun build(): ActualRoute {
            return ActualRoute(route = generateNavRoute())
        }

    }
}

abstract class RouteBuilder<R : Route> {

    protected val mandatoryArgs = mutableMapOf<String, String>()
    protected val optionalArgs = mutableMapOf<String, String?>()

    fun mandatoryArg(name: String, value: String): RouteBuilder<R> {
        this.mandatoryArgs[name] = value
        return this
    }

    fun <T : Number> mandatoryArg(name: String, value: T): RouteBuilder<R> {
        return this.mandatoryArg(name, value.toString())
    }

    fun optionalArg(name: String, value: String?): RouteBuilder<R> {
        this.optionalArgs[name] = value
        return this
    }

    fun <T : Number> optionalArg(name: String, value: T): RouteBuilder<R> {
        return this.optionalArg(name, value.toString())
    }

    abstract fun generateNavRoute(): String

    abstract fun build(): R
}