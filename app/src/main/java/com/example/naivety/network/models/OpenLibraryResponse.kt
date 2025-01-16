// app/src/main/java/com/example/naivety/network/models/OpenLibraryResponse.kt
package com.example.naivety.network.models

data class OpenLibraryResponse(
    val works: List<OpenLibraryWork>,
)

data class OpenLibraryWork(
    val key: String,
    val title: String,
    val cover_id: Long?,
    val authors: List<OpenLibraryAuthor>?,
    val first_publish_year: Int?
){
    val coverUrl: String
        get() = if (cover_id != null) {
            "https://covers.openlibrary.org/b/id/$cover_id-L.jpg"
        } else {
            "" // Provide a default cover URL
        }
}

data class OpenLibraryAuthor(
    val name: String,
    val key: String
)

