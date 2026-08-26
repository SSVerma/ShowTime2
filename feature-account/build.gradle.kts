plugins {
    id("showtime.android.feature")
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.ssverma.feature.account"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.coreStorage)
    implementation(projects.coreBilling)
    implementation(projects.coreBackup)
    implementation(projects.coreCcm)
    implementation(projects.coreNavigation)
    implementation(projects.sharedData)
    implementation(projects.apiService.tmdb)
    implementation(projects.featureAuth)
    api(projects.featureAccountNavigation)
    api(projects.featureLibraryNavigation)

    implementation(libs.datastore.preferences)
    implementation(libs.protobuf.javalite)

    testImplementation(projects.sharedTesting)
}

protobuf {
    protoc {
        artifact = libs.protoc.get().run { "$group:$name:$version" }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}
