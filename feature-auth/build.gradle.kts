plugins {
    id("showtime.android.feature")
}

android {
    namespace = "com.ssverma.feature.auth"

    packagingOptions {
        resources {
            excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation(projects.apiService.tmdb)
    implementation(projects.coreStorage)
    implementation(projects.coreBackup)
    api(projects.featureAuthNavigation)

    implementation(libs.datastore.preferences)
    implementation(libs.gson)

    testImplementation(projects.sharedTesting)
    testImplementation(projects.coreTesting)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.junit)
}
