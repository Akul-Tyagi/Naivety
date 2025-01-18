// app/src/main/java/com/example/naivety/network/models/OpenLibraryResponse.kt
package com.example.naivety.network.models

import com.google.gson.annotations.SerializedName

data class  OpenLibraryAuthor(
    @SerializedName("name")
    val name: String,
)

data class OpenLibraryResponse(
    @SerializedName("numFound")
    val numFound: Int = 0,

    @SerializedName("start")
    val start: Int = 0,

    @SerializedName("numFoundExact")
    val numFoundExact: Boolean = false,

    @SerializedName("docs")
    val docs: List<OpenLibraryDoc>? = null,

    @SerializedName("works")
    val works: List<OpenLibraryWork>? = null
)

data class OpenLibraryDoc(
    @SerializedName("key")
    val key: String?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("cover_i")
    val cover_i: Long?,

    @SerializedName("author_name")
    val author_name: List<String>?,

    @SerializedName("first_publish_year")
    val first_publish_year: Int?
)

data class OpenLibraryWork(
    @SerializedName("key")
    val key: String?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("cover_i")
    val cover_i: Long?,

    @SerializedName("author_name")
    val author_name: List<String>?,

    @SerializedName("first_publish_year")
    val first_publish_year: Int?
)