// Create a new file: app/src/main/java/com/abundance/naivety/utils/PreferencesManager.kt

package com.abundance.naivety.utils

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREF_NAME = "NaivetyPrefs"
    private const val KEY_FIRST_TIME = "isFirstTime"
    private const val KEY_FIRST_TIME_LOGIN = "isFirstTimeLogin"

    private const val KEY_GUEST_MODE = "isGuestMode" // Add this

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstTime(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_FIRST_TIME, true)
    }

    fun setFirstTimeDone(context: Context) {
        getPreferences(context).edit().putBoolean(KEY_FIRST_TIME, false).apply()
    }

    fun setFirstTimeLoginDone(context: Context) {
        getPreferences(context).edit().putBoolean(KEY_FIRST_TIME_LOGIN, false).apply()
    }

    fun isFirstTimeLogin(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_FIRST_TIME_LOGIN, true)
    }

    // Add guest mode functions
    fun setGuestMode(context: Context, isGuest: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_GUEST_MODE, isGuest).apply()
    }

    fun isGuestMode(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_GUEST_MODE, false)
    }

    fun clearGuestMode(context: Context) {
        getPreferences(context).edit().putBoolean(KEY_GUEST_MODE, false).apply()
    }
}