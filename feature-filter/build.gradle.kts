plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.feature.filter"
}

dependencies {
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
}
