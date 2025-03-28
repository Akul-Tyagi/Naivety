package com.example.naivety

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.example.naivety.repository.UserPreferencesRepository
import com.example.naivety.ads.AdManager

@HiltAndroidApp
class NaivetyApplication : Application() {
    // Lazy initialize the repository
    val userPreferencesRepository by lazy {
        val repo = UserPreferencesRepository()
        repo.initialize(applicationContext)
        repo
    }

    override fun onCreate() {
        super.onCreate()
        AdManager.initialize(this)
    }
}