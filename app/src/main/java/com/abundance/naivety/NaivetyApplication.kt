package com.abundance.naivety

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.abundance.naivety.repository.UserPreferencesRepository
import com.abundance.naivety.ads.AdManager

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