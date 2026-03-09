plugins {
    id("showtime.android.feature")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ssverma.feature.search"
}

dependencies {
    implementation(projects.coreStorage)
    implementation(projects.apiService.tmdb)

    api(projects.featureSearchNavigation)
    implementation(projects.featureMovieNavigation)
    implementation(projects.featureTvNavigation)
    implementation(projects.featurePersonNavigation)

    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
