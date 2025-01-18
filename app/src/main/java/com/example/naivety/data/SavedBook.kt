package com.example.naivety.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_books")
data class SavedBook(
    @PrimaryKey
    val bookKey: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val publishedYear: Int,
    val description: String = "",
    val dateAdded: Long = System.currentTimeMillis()
)