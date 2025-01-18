// app/src/main/java/com/example/naivety/network/models/OpenLibraryBookDetail.kt
package com.example.naivety.network.models

data class OpenLibraryBookDetail(
    val key: String,
    val title: String,
    val description: Any?,
    val number_of_pages: Int? = null,
    val authors: List<OpenLibraryAuthor>?,
    val first_publish_year: Int?,
    val covers: List<Long>?,
    val ratings_average: Float? = null,
    val ratings_count: Int? = null,
    val subjects: List<String>? = null
) {
    val pageCount: String
        get() = when {
            number_of_pages == null || number_of_pages == 0 -> "Pages unavailable"
            number_of_pages == 1 -> "1 page"
            else -> "$number_of_pages pages"
        }

    fun getDescription(): String {
        return when (description) {
            is String -> description
            is Map<*, *> -> (description as Map<*, *>)["value"] as? String ?: ""
            else -> ""
        }
    }
}