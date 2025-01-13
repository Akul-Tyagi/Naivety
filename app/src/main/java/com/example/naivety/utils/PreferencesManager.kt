// Create a new file: app/src/main/java/com/example/naivety/utils/PreferencesManager.kt

package com.example.naivety.utils

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREF_NAME = "NaivetyPrefs"
    private const val KEY_FIRST_TIME = "isFirstTime"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstTime(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_FIRST_TIME, true)
    }

    fun setFirstTimeDone(context: Context) {
        getPreferences(context).edit().putBoolean(KEY_FIRST_TIME, false).apply()
    }

    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }
}