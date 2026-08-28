plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.ssverma.shared.data"
}

dependencies {
    implementation(projects.sharedDomain)

    implementation(projects.coreNetworking)
    implementation(projects.coreStorage)
    implementation(projects.coreBackup)
    implementation(projects.corePaging)
    implementation(projects.coreCcm)

    implementation(projects.apiService.tmdb)

    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    implementation(libs.gson)
    implementation(libs.workmanager.ktx)
    implementation(libs.core.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.play.services)
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(projects.sharedTesting)
}
