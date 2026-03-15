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
    implementation(projects.coreUi)
    implementation(projects.sharedUi)
    implementation(projects.sharedData)
    implementation(projects.coreUiPaging)
    implementation(projects.coreNavigation)
    implementation(projects.sharedDomain)

    implementation(projects.coreNetworking)
    implementation(projects.corePaging)
    implementation(projects.coreDi)
    implementation(projects.apiService.tmdb)
    implementation(libs.hilt.navigation.compose)
}
