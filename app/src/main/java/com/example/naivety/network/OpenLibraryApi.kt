// app/src/main/java/com/example/naivety/network/OpenLibraryApi.kt
package com.example.naivety.network

import com.example.naivety.network.models.OpenLibraryBookDetail
import com.example.naivety.network.models.OpenLibraryResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("works/{workId}/editions.json")
    suspend fun getBookEditions(
        @Path("workId") workId: String,
        @Query("limit") limit: Int = 1,
        @Query("fields") fields: String = "number_of_pages"
    ): EditionsResponse

    @GET("works/{workId}.json")
    suspend fun getBookDetails(@Path("workId") workId: String): OpenLibraryBookDetail

        @GET("search.json")
        suspend fun searchBooks(
            @Query("q") query: String,
            @Query("page") page: Int,
            @Query("limit") limit: Int = 20,
            @Query("fields") fields: String = "key,title,cover_i,author_name,first_publish_year"
        ): OpenLibraryResponse

    @GET("trending/weekly.json")
    suspend fun getTrendingBooks(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20,
        // Only request minimal fields needed for the grid view
        @Query("fields") fields: String = "key,title,cover_i,author_name"
    ): OpenLibraryResponse

        @GET("works/{workId}/ratings.json")
        suspend fun getBookRatings(@Path("workId") workId: String): RatingsResponse
    }

data class EditionsResponse(
    val entries: List<EditionEntry>
)

data class EditionEntry(
    val number_of_pages: Int?
)

    data class RatingsResponse(
        val summary: RatingSummary
    )

    data class RatingSummary(
        val average: Float = 0f,
        val count: Int = 0
    )