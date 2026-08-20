plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ssverma.core.billing"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.coreDi)
    implementation(projects.coreAnalytics)
    implementation(projects.coreCcm)
    implementation(projects.coreStorage)

    api(libs.play.billing)
    implementation(libs.coroutines.core)

    testImplementation(projects.coreTesting)
}
