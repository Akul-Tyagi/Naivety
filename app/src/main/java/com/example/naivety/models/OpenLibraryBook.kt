// app/src/main/java/com/example/naivety/models/OpenLibraryBook.kt
package com.example.naivety.models

data class OpenLibraryBook(
    val key: String,
    val title: String,
    val coverUrl: String,
    val author: String,
    val publishedYear: Int,
    val description: String,
    val rating: Float = 0f,
    val ratingsCount: Int = 0
)