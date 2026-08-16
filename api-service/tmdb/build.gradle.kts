plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(projects.sharedDomain)
    api(projects.coreNetworking)
    implementation(projects.corePaging)

    api(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    api(libs.okhttpLoggingInterceptor)

    implementation(libs.dagger.hilt.core)
    ksp(libs.dagger.hilt.compiler)
}
