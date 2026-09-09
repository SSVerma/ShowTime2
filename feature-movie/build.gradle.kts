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
    implementation(projects.featureAccountNavigation)
    implementation(projects.featureLibraryNavigation)

    implementation(projects.featureFilter)

    implementation(projects.coreAnalytics)
    implementation(projects.coreNotifications)
    implementation(projects.coreAds)
    implementation(projects.coreBilling)
    implementation(projects.commonUi)
    implementation(projects.featurePaymentNavigation)
    implementation(projects.sharedAnalytics)
    implementation(projects.sharedAds)

    testImplementation(projects.sharedTesting)
}
