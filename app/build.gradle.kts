plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.abundance.naivety"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.abundance.naivety"
        minSdk = 26
        targetSdk = 35
        versionCode = 14
        versionName = "3.7.1"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("C:\\Users\\Vishesh\\my_release_key.keystore.jks")
            storePassword = "@Cool2005"
            keyAlias = "keynaivety"
            keyPassword = "@Cool2005"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true  // Enable this
            isShrinkResources = true  // Add this
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    viewBinding {
        enable = true }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Firebase (using BOM for version management)
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.5.0")

    // Compose core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.core:core-splashscreen:1.2.0")

    // Network
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // PDF viewer
    implementation("com.github.Akul-Tyagi:AndroidPdfViewer:v3.4.3-16kb")

    // AdMob
    implementation("com.google.android.gms:play-services-ads:24.9.0")
    
    // Paging 3 with Compose support
    implementation("androidx.paging:paging-compose:3.3.6")

    // Dagger Hilt for dependency injection
    implementation("com.google.dagger:hilt-android:2.58")
    ksp("com.google.dagger:hilt-android-compiler:2.58")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation("androidx.navigation:navigation-compose:2.9.6")

    // Compose animations and foundation
    implementation("androidx.compose.animation:animation:1.10.1")
    implementation("androidx.compose.foundation:foundation:1.10.1")

    // System UI controller
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    // Room database
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Readium (EPUB only)
    implementation("org.readium.kotlin-toolkit:readium-shared:3.1.2")
    implementation("org.readium.kotlin-toolkit:readium-streamer:3.1.2")
    implementation("org.readium.kotlin-toolkit:readium-navigator:3.1.2")

    // Required by Readium navigator for embedded web rendering.
    implementation("androidx.webkit:webkit:1.12.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}