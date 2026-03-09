plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.shared.data"
}

dependencies {
    implementation(projects.sharedDomain)
    implementation(projects.coreNetworking)
    implementation(projects.coreStorage)
    implementation(projects.apiService.tmdb)

    implementation(libs.core.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.datastore.preferences)
}
