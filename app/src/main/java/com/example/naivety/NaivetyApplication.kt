// Updated NaivetyApplication.kt
package com.example.naivety

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.example.naivety.repository.UserPreferencesRepository

@HiltAndroidApp
class NaivetyApplication : Application() {
    // Lazy initialize the repository
    val userPreferencesRepository by lazy {
        val repo = UserPreferencesRepository()
        repo.initialize(applicationContext)
        repo
    }
}