package com.ssverma

object Modules {
    object Core {
        const val networking = ":core-networking"
        const val paging = ":core-paging"
        const val ui = ":core-ui"
        const val uiPaging = ":core-ui-paging"
        const val navigation = ":core-navigation"
        const val di = ":core-di"
        const val image = ":core-image"
        const val storage = ":core-storage"
    }

    object Shared {
        const val ui = ":shared-ui"
        const val domain = ":shared-domain"
        const val data = ":shared-data"
    }

    object ApiService {
        const val tmdb = ":api-service:tmdb"
    }

    object Feature {
        const val movie = ":feature-movie"
        const val movieNavigation = ":feature-movie-navigation"
        const val person = ":feature-person"
        const val personNavigation = ":feature-person-navigation"
        const val tv = ":feature-tv"
        const val tvNavigation = ":feature-tv-navigation"
        const val library = ":feature-library"
        const val libraryNavigation = ":feature-library-navigation"
        const val filter = ":feature-filter"
        const val search = ":feature-search"
        const val searchNavigation = ":feature-search-navigation"
        const val auth = ":feature-auth"
        const val authNavigation = ":feature-auth-navigation"
        const val account = ":feature-account"
        const val accountNavigation = ":feature-account-navigation"
    }
}