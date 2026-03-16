plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.coroutines.core)
    implementation(libs.javax.inject)
    implementation(projects.corePaging)
}
