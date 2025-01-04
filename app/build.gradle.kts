plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.naivety"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.naivety"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        dataBinding= true
    }

    viewBinding {
        enable = true }
}

dependencies {
    implementation ("com.airbnb.android:lottie:5.2.0")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation ("androidx.compose.ui:ui:1.5.0")
    implementation ("androidx.compose.runtime:runtime-livedata:1.5.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material3:material3:1.4.0-alpha04")
    implementation ("com.google.android.material:material:1.11.0-alpha01")
    implementation("androidx.compose.material3:material3-window-size-class:1.4.0-alpha04")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.4.0-alpha04")
    implementation("androidx.core:core-splashscreen:1.0.0") // Add this line
    implementation("com.google.firebase:firebase-auth-ktx:22.0.0") // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore-ktx:24.4.0") // Firebase Firestore
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Retrofit Gson Converter
    implementation("io.coil-kt:coil-compose:2.2.2") // Image loading with Coil
    implementation ("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}