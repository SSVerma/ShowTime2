package com.ssverma.showtime.navigation

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

    fun asRoutableString(): String {
        return builtRoute
    }
}

class PlaceholderRoute private constructor(
    route: String
) : Route(route) {

    class PlaceHolderRouteBuilder internal constructor(
        private val route: String
    ) {
        private val mandatoryArgs = mutableSetOf<String>()
        private val optionalArgs = mutableSetOf<String>()

        fun mandatoryArg(arg: String): PlaceHolderRouteBuilder {
            this.mandatoryArgs.add(arg)
            return this
        }

        fun optionalArg(arg: String): PlaceHolderRouteBuilder {
            this.optionalArgs.add(arg)
            return this
        }

        private fun generateRoute(): String {
            var completeRoute = mandatoryArgs.joinToString(
                separator = "/",
                transform = { "{$it}" },
                prefix = if (mandatoryArgs.isEmpty()) {
                    route
                } else {
                    "$route/"
                },
            )

            optionalArgs.forEachIndexed { index, arg ->
                completeRoute = if (index == 0) {
                    "$completeRoute?$arg={$arg}"
                } else {
                    "$completeRoute&$arg={$arg}"
                }
            }

            return completeRoute
        }

        fun build(): PlaceholderRoute {
            return PlaceholderRoute(route = generateRoute())
        }
    }
}

class ActualRoute private constructor(
    route: String
) : Route(route) {

    class ActualRouteBuilder(
        private val route: String
    ) {
        private val mandatoryArgs = mutableMapOf<String, String>()
        private val optionalArgs = mutableMapOf<String, String?>()

        fun mandatoryArg(key: String, value: String): ActualRouteBuilder {
            this.mandatoryArgs[key] = value
            return this
        }

        fun <T : Number> mandatoryArg(key: String, value: T): ActualRouteBuilder {
            return this.mandatoryArg(key, value.toString())
        }

        fun optionalArg(key: String, value: String?): ActualRouteBuilder {
            this.optionalArgs[key] = value
            return this
        }

        fun <T : Number> optionalArg(key: String, value: T): ActualRouteBuilder {
            return this.optionalArg(key, value.toString())
        }

        private fun generateRoute(): String {
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

        fun build(): ActualRoute {
            return ActualRoute(route = generateRoute())
        }

    }
}