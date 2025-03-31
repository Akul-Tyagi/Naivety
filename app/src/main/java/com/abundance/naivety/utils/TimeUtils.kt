// app/src/main/java/com/abundance/naivety/utils/TimeUtils.kt
package com.abundance.naivety.utils

import java.time.Month
import java.util.Locale

/**
 * Extension function to get the display name of a Month in a compatible way
 */
fun Month.getDisplayNameCompat(): String {
    return this.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
}