package com.ssverma

object Modules {
    object Core {
        const val networking = ":core-networking"
        const val paging = ":core-paging"
        const val ui = ":core-ui"
        const val domain = ":core-domain"
        const val uiPaging = ":core-ui-paging"
    }

    object Shared {
        const val ui = ":shared-ui"
    }

    object ApiService {
        const val tmdb = ":api-service:tmdb"
    }
}