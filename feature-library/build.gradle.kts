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

    implementation(projects.featureAccount)
    implementation(projects.featureAuth)
}
