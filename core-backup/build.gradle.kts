plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ssverma.core.backup"
}

dependencies {
    implementation(projects.coreDi)
    implementation(projects.coreStorage)
    implementation(projects.coreAnalytics)

    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.workmanager.ktx)
    implementation(libs.gson)
    implementation(libs.coroutines.core)
    implementation(libs.datastore.preferences)

    testImplementation(projects.coreTesting)
}
