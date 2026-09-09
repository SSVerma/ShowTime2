plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.feature.filter"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(projects.featureFilterNavigation)
    implementation(projects.featureMovieNavigation)
    implementation(projects.featureTvNavigation)
    implementation(projects.featureCommunityNavigation)

    implementation(projects.coreUi)
    implementation(projects.commonUi)
    implementation(projects.sharedUi)
    implementation(projects.coreUiPaging)
    implementation(projects.coreNavigation)
    implementation(projects.sharedDomain)
    implementation(projects.featureLibraryNavigation)
    implementation(projects.sharedAds)
    implementation(projects.coreAds)
    implementation(projects.coreBilling)
    implementation(projects.featurePaymentNavigation)

    implementation(projects.coreNetworking)
    implementation(projects.corePaging)
    implementation(projects.coreDi)
    implementation(projects.coreImage)
    implementation(libs.hilt.navigation.compose)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(projects.coreTesting)
    testImplementation(projects.sharedTesting)
}
