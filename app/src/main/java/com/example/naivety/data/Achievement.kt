// app/src/main/java/com/example/naivety/data/Achievement.kt
package com.example.naivety.data

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val unlocked: Boolean = false,
    val dateUnlocked: Long? = null
)