// app/src/main/java/com/example/naivety/network/models/OpenLibraryResponse.kt
package com.example.naivety.network.models

data class OpenLibraryResponse(
    val works: List<OpenLibraryWork>,
)

data class OpenLibraryWork(
    val key: String,
    val title: String,
    val cover_i: Long?,
    val author_name: List<String>?,
    val first_publish_year: Int?
){
    val coverUrl: String
        get() = if (cover_i != null) {
            "https://covers.openlibrary.org/b/id/$cover_i-L.jpg"
        } else {
            "" // Provide a default cover URL
        }
}

data class OpenLibraryAuthor(
    val name: String,
    val key: String
)

