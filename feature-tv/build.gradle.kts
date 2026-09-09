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
    implementation(projects.featureAccountNavigation)
    implementation(projects.featureLibraryNavigation)

    implementation(projects.featureFilter)
    implementation(projects.featureAuth)

    implementation(projects.coreAnalytics)
    implementation(projects.coreNotifications)
    implementation(projects.coreBilling)
    implementation(projects.coreAds)
    implementation(projects.featurePayment)
    implementation(projects.featurePaymentNavigation)
    implementation(projects.sharedAnalytics)
    implementation(projects.sharedAds)

    testImplementation(projects.sharedTesting)
}
