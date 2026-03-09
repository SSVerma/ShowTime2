import com.ssverma.AndroidConfig
import com.ssverma.Modules
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.compose.compiler)
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
    compileSdk = AndroidConfig.CompileSdk

    val FIELD_TMDB_BASE_URL = "TMDB_BASE_URL"
    val FIELD_TMDB_API_READ_ACCESS_TOKEN = "TMDB_API_READ_ACCESS_TOKEN"

    defaultConfig {
        applicationId = "com.ssverma.showtime"
        minSdk = AndroidConfig.MinSdk
        targetSdk = AndroidConfig.TargetSdk
        versionCode = 8
        versionName = "1.0.4"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "META-INF/gradle/incremental.annotation.processors"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(Modules.ApiService.tmdb))
    implementation(project(Modules.Core.ui))
    implementation(project(Modules.Core.navigation))
    implementation(project(Modules.Core.networking))

    implementation(project(Modules.Shared.ui))
    implementation(project(Modules.Shared.domain))

    implementation(project(Modules.Feature.movie))
    implementation(project(Modules.Feature.tv))
    implementation(project(Modules.Feature.person))
    implementation(project(Modules.Feature.library))
    implementation(project(Modules.Feature.search))
    implementation(project(Modules.Feature.auth))
    implementation(project(Modules.Feature.account))

    implementation(libs.material)
    implementation(libs.compose.activity)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}