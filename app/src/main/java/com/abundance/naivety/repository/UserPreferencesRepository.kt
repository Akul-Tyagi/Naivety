// Updated UserPreferencesRepository.kt
package com.abundance.naivety.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor() {
    companion object {
        private const val PREF_NAME = "user_preferences"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_FIRST_TIME = "first_time"
    }

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstTime(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_FIRST_TIME, true)
    }

    fun setFirstTimeDone(context: Context) {
        getPreferences(context).edit().putBoolean(KEY_FIRST_TIME, false).apply()
    }

    fun initialize(context: Context) {
        _isDarkTheme.value = getPreferences(context).getBoolean(KEY_DARK_THEME, true)
    }

    fun setDarkTheme(context: Context, isDark: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_DARK_THEME, isDark).apply()
        _isDarkTheme.value = isDark
    }
}