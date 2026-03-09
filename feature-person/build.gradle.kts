plugins {
    id("showtime.android.feature")
}

android {
    namespace = "com.ssverma.feature.person"
}

dependencies {
    implementation(projects.apiService.tmdb)

    api(projects.featurePersonNavigation)
    implementation(projects.featureMovieNavigation)
    implementation(projects.featureTvNavigation)
    implementation(projects.featureSearchNavigation)
    implementation(projects.featureAccountNavigation)

    implementation(libs.compose.constraintlayout)
}