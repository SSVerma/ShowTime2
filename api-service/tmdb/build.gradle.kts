plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    implementation(projects.sharedDomain)
    implementation(projects.coreNetworking)
    implementation(projects.corePaging)

    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttpLoggingInterceptor)

    implementation(libs.dagger.hilt.core)
    kapt(libs.dagger.hilt.compiler)
}