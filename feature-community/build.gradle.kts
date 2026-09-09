plugins {
    id("showtime.android.feature")
}

android {
    namespace = "com.ssverma.feature.community"
}

dependencies {
    api(projects.featureCommunityNavigation)
    implementation(projects.featureMovieNavigation)
    implementation(projects.featureTvNavigation)

    implementation(projects.coreBackup)
    implementation(projects.coreCcm)
    implementation(projects.coreStorage)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)
    implementation(libs.datastore.preferences)

    testImplementation(projects.sharedTesting)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
}
