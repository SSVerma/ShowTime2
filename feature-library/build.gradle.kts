plugins {
    id("showtime.android.feature")
}

android {
    namespace = "com.ssverma.feature.library"
}

dependencies {
    implementation(projects.apiService.tmdb)

    api(projects.featureLibraryNavigation)
    implementation(projects.featurePersonNavigation)
    implementation(projects.featureSearchNavigation)
    implementation(projects.featureMovieNavigation)
    implementation(projects.featureTvNavigation)
    implementation(projects.featureAccountNavigation)
    implementation(projects.featurePaymentNavigation)

    testImplementation(libs.junit)
    testImplementation(projects.coreTesting)
    testImplementation(projects.sharedTesting)
}
