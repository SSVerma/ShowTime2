plugins {
    id("showtime.android.feature")
}

android {
    namespace = "com.ssverma.feature.movie"
}

dependencies {
    implementation(projects.apiService.tmdb)

    api(projects.featureMovieNavigation)
    implementation(projects.featurePersonNavigation)
    implementation(projects.featureSearchNavigation)
    implementation(projects.featureFilterNavigation)

    implementation(projects.featureFilter)
    implementation(projects.featureSearch)
    implementation(projects.featureAccount)

    implementation(projects.coreAnalytics)
    implementation(projects.coreNotifications)
    implementation(projects.coreAds)
    implementation(projects.sharedAnalytics)
    implementation(projects.sharedAds)

    testImplementation(projects.sharedTesting)
}
