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
        versionCode = 10
        versionName = "2.2.6"
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
    implementation ("com.airbnb.android:lottie:6.2.0")
    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation ("androidx.compose.ui:ui:1.7.8")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.material3:material3:1.4.0-alpha10")
    implementation ("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.4.0-alpha10")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.4.0-alpha10")
    implementation("androidx.core:core-splashscreen:1.0.1") // Add this line
    implementation("com.google.firebase:firebase-auth-ktx:23.2.0") // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.3") // Firebase Firestore
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Retrofit Gson Converter
    implementation("io.coil-kt:coil-compose:2.5.0") // Image loading with Coil
    // Replace the existing PDF viewer dependency with this
    implementation("com.github.Akul-Tyagi:AndroidPdfViewer:v3.2.5")

    //admob
    implementation("com.google.android.gms:play-services-ads:24.1.0")
    
    //Browse Section
    // Paging 3 with Compose support
    implementation("androidx.paging:paging-compose:3.3.6")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Dagger Hilt for dependency injection
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.0-1.0.29")
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    // Additional Compose dependencies for animations and effects
    implementation("androidx.compose.animation:animation:1.7.8")
    implementation("androidx.compose.foundation:foundation:1.7.6")
    // For blur effects and other UI utilities
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("com.google.accompanist:accompanist-placeholder:0.32.0")
    // Add for better PDF handling
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.8")
    implementation(libs.books)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}