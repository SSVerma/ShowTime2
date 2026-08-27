plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.feature.filter"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(projects.featureFilterNavigation)
    implementation(projects.featureMovieNavigation)
    implementation(projects.featureTvNavigation)

    implementation(projects.coreUi)
    implementation(projects.sharedUi)
    implementation(projects.sharedData)
    implementation(projects.coreUiPaging)
    implementation(projects.coreNavigation)
    implementation(projects.sharedDomain)
    implementation(projects.featureAccount)
    implementation(projects.featureLibraryNavigation)
    implementation(projects.sharedAds)
    implementation(projects.coreAds)

    implementation(projects.coreNetworking)
    implementation(projects.corePaging)
    implementation(projects.coreDi)
    implementation(projects.apiService.tmdb)
    implementation(projects.coreImage)
    implementation(libs.hilt.navigation.compose)
}
