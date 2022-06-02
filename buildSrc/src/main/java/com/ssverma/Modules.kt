package com.ssverma

object Modules {
    object Core {
        const val networking = ":core-networking"
        const val paging = ":core-paging"
        const val ui = ":core-ui"
        const val domain = ":core-domain"
        const val uiPaging = ":core-ui-paging"
        const val navigation = ":core-navigation"
        const val di = ":core-di"
    }

    object Shared {
        const val ui = ":shared-ui"
        const val data = ":shared-data"
    }

    object ApiService {
        const val tmdb = ":api-service:tmdb"
    }

    object Feature {
        const val movie = ":feature-movie"
        const val filter = ":feature-filter"
    }
}