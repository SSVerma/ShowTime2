plugins {
    id("showtime.android.feature")
}

android {
    namespace = "com.ssverma.feature.match"
}

dependencies {
    api(projects.featureMatchNavigation)
    implementation(projects.featureMovieNavigation)
    implementation(projects.featurePaymentNavigation)

    implementation(projects.coreBilling)
    implementation(projects.coreStorage)
    implementation(projects.coreImage)
    implementation(projects.coreUi)
    implementation(projects.sharedData)
    implementation(projects.sharedAds)
    implementation(projects.apiService.tmdb)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.coroutines.play.services)
    implementation(libs.gson)
    implementation(libs.datastore.preferences)

    testImplementation(projects.sharedTesting)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
}
