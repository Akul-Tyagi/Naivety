package com.abundance.naivety.network

import com.abundance.naivety.network.models.OpenLibraryBookDetail
import com.abundance.naivety.network.models.OpenLibrarySearchResponse
import com.abundance.naivety.network.models.RatingsResponse
import com.abundance.naivety.network.models.TrendingBooksResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("fields") fields: String = "key,title,author_name,first_publish_year,cover_i"
    ): Response<OpenLibrarySearchResponse>

    @GET("trending/weekly.json")
    suspend fun getTrendingBooks(): Response<TrendingBooksResponse>

    @GET("works/{workId}.json")
    suspend fun getBookDetails(
        @Path("workId") workId: String,
    ): Response<OpenLibraryBookDetail>

    // Add this new endpoint
    @GET("works/{workId}/ratings.json")
    suspend fun getBookRatings(@Path("workId") workId: String): Response<RatingsResponse>

    @GET("works/{workId}/editions.json")
    suspend fun getBookEditions(
        @Path("workId") workId: String,
        @Query("limit") limit: Int = 10
    ): Response<OpenLibraryBookDetail>

    companion object {
        private const val BASE_URL = "https://openlibrary.org/"

        fun create(): OpenLibraryApi {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Accept", "application/json")
                        .header("Cache-Control", "max-age=600")
                        .build()
                    chain.proceed(request)
                }
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(OpenLibraryApi::class.java)
        }
    }
}