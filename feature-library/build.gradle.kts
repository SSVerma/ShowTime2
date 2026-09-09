plugins {
    id("showtime.android.feature")
}

android {
    namespace = "com.ssverma.feature.library"
}

dependencies {
    implementation(projects.apiService.tmdb)

    api(projects.featureLibraryNavigation)
    implementation(projects.sharedAds)
    implementation(projects.coreBilling)
    implementation(projects.featurePersonNavigation)
    implementation(projects.featureSearchNavigation)
    implementation(projects.featureMovieNavigation)
    implementation(projects.featureTvNavigation)
    implementation(projects.featureAccountNavigation)
    implementation(projects.commonUi)
    implementation(projects.featurePaymentNavigation)
    implementation(projects.coreBackup)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(projects.coreTesting)
    testImplementation(projects.sharedTesting)
}
