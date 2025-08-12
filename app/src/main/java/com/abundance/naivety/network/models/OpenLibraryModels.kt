package com.abundance.naivety.network.models

data class OpenLibrarySearchResponse(
    val numFound: Int,
    val start: Int,
    val docs: List<OpenLibraryDoc>
)

data class RatingsResponse(
    val summary: RatingSummary?,
    val counts: Map<String, Int>?
)

data class RatingSummary(
    val average: Float?,
    val count: Int?,
    val sortable: Float?
)

data class OpenLibraryDoc(
    val key: String?,
    val title: String?,
    val author_name: List<String>?,
    val first_publish_year: Int?,
    val cover_i: Int?
)

data class OpenLibraryWork(
    val key: String?,
    val title: String?,
    val author_name: List<String>?,
    val first_publish_year: Int?,
    val cover_i: Int?
)

data class OpenLibraryAuthor(
    val key: String,
    val name: String
)