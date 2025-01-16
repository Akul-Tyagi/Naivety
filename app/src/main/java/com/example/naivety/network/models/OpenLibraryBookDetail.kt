// app/src/main/java/com/example/naivety/network/models/OpenLibraryBookDetail.kt
package com.example.naivety.network.models

data class OpenLibraryBookDetail(
    val key: String,
    val title: String,
    val description: Any?,
    val authors: List<OpenLibraryAuthor>?,
    val first_publish_year: Int?,
    val covers: List<Long>?,
    val ratings_average: Float? = null,
    val ratings_count: Int? = null,
    val subjects: List<String>? = null // Add this for genres
) {
    fun getDescription(): String {
        return when (description) {
            is String -> description
            is Map<*, *> -> (description as Map<*, *>)["value"] as? String ?: ""
            else -> ""
        }
    }
}