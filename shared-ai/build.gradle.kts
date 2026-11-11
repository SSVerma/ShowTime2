plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.ssverma.shared.ai"
}

dependencies {
    implementation(projects.coreDi)
    implementation(projects.coreAi)
    implementation(projects.coreStorage)
    implementation(projects.sharedDomain)

    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.appfunctions)
    ksp(libs.appfunctions.compiler)

    testImplementation(projects.coreTesting)
}
