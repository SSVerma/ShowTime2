plugins {
    id("showtime.android.feature")
}

android {
    namespace = "com.ssverma.feature.payment"
}

dependencies {
    api(projects.featurePaymentNavigation)

    implementation(projects.coreBilling)
    implementation(projects.coreCcm)
    implementation(projects.coreAnalytics)
    implementation(projects.sharedUi)

    testImplementation(projects.coreTesting)
    testImplementation(projects.sharedTesting)
}
