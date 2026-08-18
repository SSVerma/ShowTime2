import java.util.Properties

plugins {
    id("com.android.application")
    id("com.android.built-in-kotlin")
    id("com.android.legacy-kapt")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
}

val releaseProps by lazy {
    Properties().apply {
        val file = file("../release/release.properties")
        if (file.canRead()) {
            file.inputStream().use { load(it) }
        }
    }
}

val coreProps by lazy {
    Properties().apply {
        val file = file("../core.properties")
        if (file.canRead()) {
            file.inputStream().use { load(it) }
        }
    }
}

val debugProps by lazy {
    Properties().apply {
        val file = file("../debug.properties")
        if (file.canRead()) {
            file.inputStream().use { load(it) }
        }
    }
}

android {
    namespace = "com.ssverma.showtime"
    compileSdk = libs.versions.compileSdk.get().toInt()

    val FIELD_TMDB_BASE_URL = "TMDB_BASE_URL"
    val FIELD_TMDB_API_READ_ACCESS_TOKEN = "TMDB_API_READ_ACCESS_TOKEN"

    defaultConfig {
        applicationId = "com.ssverma.showtime"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 16
        versionName = "1.0.11"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("releaseConfig") {
            releaseProps["keyAlias"]?.let { keyAlias = it as String }
            releaseProps["keyPassword"]?.let { keyPassword = it as String }
            releaseProps["keyStoreFilePath"]?.let { storeFile = file(it as String) }
            releaseProps["keyStorePassword"]?.let { storePassword = it as String }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("releaseConfig")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(
                type = "String",
                name = FIELD_TMDB_BASE_URL,
                value = releaseProps.getProperty("baseUrl")
            )
            buildConfigField(
                type = "String",
                name = FIELD_TMDB_API_READ_ACCESS_TOKEN,
                value = coreProps.getProperty("tmdbApiReadAccessToken")
            )
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            buildConfigField(
                type = "String",
                name = FIELD_TMDB_BASE_URL,
                value = debugProps.getProperty("baseUrl")
            )
            buildConfigField(
                type = "String",
                name = FIELD_TMDB_API_READ_ACCESS_TOKEN,
                value = coreProps.getProperty("tmdbApiReadAccessToken")
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "META-INF/gradle/incremental.annotation.processors"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}


dependencies {
    implementation(projects.apiService.tmdb)
    implementation(projects.coreUi)
    implementation(projects.coreNavigation)
    implementation(projects.coreNetworking)
    implementation(projects.coreAnalytics)
    implementation(projects.coreNotifications)
    implementation(projects.coreCcm)
    implementation(projects.coreAds)

    implementation(projects.sharedUi)
    implementation(projects.sharedDomain)
    implementation(projects.commonUi)

    implementation(projects.featureMovie)
    implementation(projects.featureTv)
    implementation(projects.featurePerson)
    implementation(projects.featureLibrary)
    implementation(projects.featureSearch)
    implementation(projects.featureAuth)
    implementation(projects.featureAccount)
    implementation(projects.featureFilter)

    implementation(libs.material)
    implementation(libs.compose.activity)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
