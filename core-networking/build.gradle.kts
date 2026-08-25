plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    api(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    api(libs.okhttpLoggingInterceptor)
    api(libs.okhttp.dnsoverhttps)

    implementation(libs.coroutines.core)

    implementation(libs.dagger.hilt.core)
    ksp(libs.dagger.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.retrofitConverterMoshi)
}
