// In data/Achievement.kt (create this file if it doesn't exist)
package com.abundance.naivety.data

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean = false,
    val progress: Float = 0f,
    val icon: String = "",
    val iconName: String = "",
    val dateUnlocked: Long = 0L
)