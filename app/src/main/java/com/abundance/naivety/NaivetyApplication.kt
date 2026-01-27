package com.abundance.naivety

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.abundance.naivety.repository.UserPreferencesRepository
import com.abundance.naivety.epub.ReadiumManager
import javax.inject.Inject

@HiltAndroidApp
class NaivetyApplication : Application() {
    // Lazy initialize the repository
    val userPreferencesRepository by lazy {
        val repo = UserPreferencesRepository()
        repo.initialize(applicationContext)
        repo
    }

    @Inject
    lateinit var readiumManager: ReadiumManager

    companion object {
        lateinit var instance: NaivetyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

// Extension function for accessing ReadiumManager
interface ReadiumApp {
    val readiumManager: ReadiumManager
}