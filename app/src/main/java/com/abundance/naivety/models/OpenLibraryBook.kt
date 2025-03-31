// app/src/main/java/com/abundance/naivety/models/OpenLibraryBook.kt
package com.abundance.naivety.models

data class OpenLibraryBook(
    val key: String,
    val title: String,
    val coverUrl: String,
    val author: String,
    val publishedYear: Int,
    val description: String,
    val rating: Float = 0f,
    val ratingsCount: Int = 0,
    val pageCount: Int? = null

)