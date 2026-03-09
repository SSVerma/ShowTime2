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

    implementation(projects.featureFilter)
    implementation(projects.featureAccount)
}
