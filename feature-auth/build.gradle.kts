plugins {
    id("showtime.android.feature")
}

android {
    namespace = "com.ssverma.feature.auth"

    packagingOptions {
        resources {
            excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation(projects.apiService.tmdb)
    implementation(projects.coreStorage)
    api(projects.featureAuthNavigation)

    implementation(libs.datastore.preferences)
}
