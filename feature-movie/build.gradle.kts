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

    implementation(projects.featureFilter)
    implementation(projects.featureAccount)
}
