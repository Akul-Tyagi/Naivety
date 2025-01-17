// app/src/main/java/com/example/naivety/network/OpenLibraryApi.kt
package com.example.naivety.network

import com.example.naivety.network.models.OpenLibraryBookDetail
import com.example.naivety.network.models.OpenLibraryResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("subjects/{subject}.json")
    suspend fun getBooksBySubject(
        @Path("subject") subject: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "key,title,cover_i,authors" // Add this parameter
    ): OpenLibraryResponse

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
        @Query("fields") fields: String = "key,title,cover_i,author_name,first_publish_year"
    ): OpenLibraryResponse

    @GET("works/{workId}/ratings.json")
    suspend fun getBookRatings(@Path("workId") workId: String): RatingsResponse
}

data class RatingsResponse(
    val summary: RatingSummary
)

data class RatingSummary(
    val average: Float = 0f,
    val count: Int = 0
)