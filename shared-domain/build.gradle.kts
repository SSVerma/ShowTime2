plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.coroutines.core)
    implementation(libs.javax.inject)
    implementation(projects.corePaging)

    implementation(libs.kotlinx.serialization.json)
}
