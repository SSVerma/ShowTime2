package com.ssverma

object Versions {
    const val core = "1.8.0"
    const val appcompat = "1.4.2"
    const val material = "1.6.1"
    const val compose = "1.2.0-rc02"
    const val accompanist = "0.24.12-rc"
    const val Coroutines = "1.6.1"

    object Lifecycle {
        const val runtime = "2.4.1"
        const val viewModel = "2.4.1"
    }

    object ComposeSupport {
        const val activity = "1.4.0"
        const val constraintLayout = "1.0.0"
        const val navigation = "2.5.0-beta01"
        const val paging = "1.0.0-alpha15"
        const val coil = "2.1.0"
    }

    object Di {
        const val daggerHilt = "2.42"
        const val hiltCompiler = "1.0.0"
        const val hiltCompose = "1.0.0"
    }

    object Paging {
        const val common = "3.1.1"
    }

    object Storage {
        const val room = "2.4.2"
        const val dataStore = "1.0.0"
    }

    object Desugaring {
        const val jdk = "1.1.5"
    }

    object Test {
        object Local {
            const val junit = "4.13.2"
        }

        object Instrument {
            const val junit = "1.1.3"
        }
    }
}

object Deps {
    const val coreKtx = "androidx.core:core-ktx:${Versions.core}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.Coroutines}"

    object Compose {
        const val ui = "androidx.compose.ui:ui:${Versions.compose}"
        const val material = "androidx.compose.material:material:${Versions.compose}"
        const val tooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
    }

    object ComposeSupport {
        const val activity =
            "androidx.activity:activity-compose:${Versions.ComposeSupport.activity}"
        const val constraintLayout =
            "androidx.constraintlayout:constraintlayout-compose:${Versions.ComposeSupport.constraintLayout}"
        const val navigation =
            "androidx.navigation:navigation-compose:${Versions.ComposeSupport.navigation}"
        const val paging = "androidx.paging:paging-compose:${Versions.ComposeSupport.paging}"
        const val coil = "io.coil-kt:coil-compose:${Versions.ComposeSupport.coil}"
    }

    object Lifecycle {
        const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.Lifecycle.runtime}"
        const val viewModel =
            "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.Lifecycle.viewModel}"
    }

    object Accompanist {
        const val pager = "com.google.accompanist:accompanist-pager:${Versions.accompanist}"
        const val pagerIndicators =
            "com.google.accompanist:accompanist-pager-indicators:${Versions.accompanist}"
        const val navAnimation =
            "com.google.accompanist:accompanist-navigation-animation:${Versions.accompanist}"
    }

    object Di {
        const val daggerHilt = "com.google.dagger:hilt-android:${Versions.Di.daggerHilt}"
        const val daggerHiltCore = "com.google.dagger:hilt-core:${Versions.Di.daggerHilt}"
        const val hiltCompose = "androidx.hilt:hilt-navigation-compose:${Versions.Di.hiltCompose}"

        object Kapt {
            const val daggerHiltCompiler =
                "com.google.dagger:hilt-compiler:${Versions.Di.daggerHilt}"
            const val hiltCompiler = "androidx.hilt:hilt-compiler:${Versions.Di.hiltCompiler}"
        }
    }

    object Paging {
        const val common = "androidx.paging:paging-common-ktx:${Versions.Paging.common}"
    }

    object Storage {
        const val room = "androidx.room:room-ktx:${Versions.Storage.room}"
        const val dataStorePreferences =
            "androidx.datastore:datastore-preferences:${Versions.Storage.dataStore}"
        const val dataStoreProto = "androidx.datastore:datastore:${Versions.Storage.dataStore}"

        object Kapt {
            const val room = "androidx.room:room-compiler:${Versions.Storage.room}"
        }
    }

    object Desugaring {
        const val jdk = "com.android.tools:desugar_jdk_libs:${Versions.Desugaring.jdk}"
    }

    object Test {
        object Local {
            const val junit = "junit:junit:${Versions.Test.Local.junit}"
        }

        object Instrument {
            const val junit = "androidx.test.ext:junit:${Versions.Test.Instrument.junit}"
        }
    }
}