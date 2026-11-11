plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.core.ai"
}

dependencies {
    implementation(projects.coreDi)

    implementation(libs.coroutines.core)
    implementation(libs.mlkit.genai.agent)
    implementation(libs.mlkit.genai.prompt)
    implementation(libs.appfunctions)

    testImplementation(projects.coreTesting)
}
