plugins {
    id("showtime.android.feature")
}

android {
    namespace = "com.ssverma.feature.tv"
}

dependencies {
    implementation(projects.apiService.tmdb)

    api(projects.featureTvNavigation)
    implementation(projects.featureMovieNavigation)
    implementation(projects.featurePersonNavigation)
    implementation(projects.featureSearchNavigation)
    implementation(projects.featureFilterNavigation)

    implementation(projects.featureFilter)
    implementation(projects.featureSearch)
    implementation(projects.featureAccount)
    implementation(projects.featureAuth)

    implementation(projects.coreAnalytics)
    implementation(projects.coreNotifications)
    implementation(projects.sharedAnalytics)
    implementation(projects.sharedAds)
}
