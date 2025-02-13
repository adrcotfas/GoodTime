plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.mikepenz.aboutlibraries)
}

android {
    val packageName = "com.apps.adrcotfas.goodtime"
    namespace = packageName
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = packageName
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 300
        versionName = "3.0.0"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "19"
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }

    aboutLibraries {
        configPath = "androidApp/config"
    }
}

dependencies {
    implementation(libs.androidx.documentfile)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(projects.shared)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.media)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.mikepenz.aboutlibraries.core)
    implementation(libs.mikepenz.aboutlibraries.compose)
    implementation(libs.devsrsouza.compose.icons.eva)

    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.androidchart)

    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
}
