// app/src/main/java/com/example/naivety/network/OpenLibraryApi.kt
package com.example.naivety.network

import com.example.naivety.network.models.OpenLibraryBookDetail
import com.example.naivety.network.models.OpenLibraryResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenLibraryApi {
    @GET("subjects/{subject}.json")
    suspend fun getBooksBySubject(@Path("subject") subject: String): OpenLibraryResponse

    @GET("works/{workId}.json")
    suspend fun getBookDetails(@Path("workId") workId: String): OpenLibraryBookDetail
}