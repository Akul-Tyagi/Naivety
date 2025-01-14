// app/src/main/java/com/example/naivety/network/models/OpenLibraryResponse.kt
data class OpenLibraryResponse(
    val works: List<OpenLibraryWork>,
    val nextPage: String?
)

data class OpenLibraryWork(
    val key: String,
    val title: String,
    val cover_id: Long?,
    val authors: List<OpenLibraryAuthor>?,
    val first_publish_year: Int?
)

data class OpenLibraryAuthor(
    val name: String,
    val key: String
)

