// app/src/main/java/com/example/naivety/network/models/OpenLibraryBookDetail.kt
data class OpenLibraryBookDetail(
    val key: String,
    val title: String,
    val description: Description?,
    val authors: List<OpenLibraryAuthor>?,
    val first_publish_year: Int?,
    val covers: List<Long>?
) {
    data class Description(
        val value: String?,
        val type: String?
    )
}