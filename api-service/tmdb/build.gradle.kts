plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(projects.sharedDomain)
    implementation(projects.coreNetworking)
    implementation(projects.corePaging)

    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttpLoggingInterceptor)

    implementation(libs.dagger.hilt.core)
    ksp(libs.dagger.hilt.compiler)
}