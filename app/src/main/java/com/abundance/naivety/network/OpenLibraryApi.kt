package com.abundance.naivety.network

import com.abundance.naivety.network.models.OpenLibraryBookDetail
import com.abundance.naivety.network.models.OpenLibrarySearchResponse
import com.abundance.naivety.network.models.RatingsResponse
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("fields") fields: String = "key,title,author_name,first_publish_year,cover_i",
        @Query("mode") mode: String = "bestseller",
        @Query("has_fulltext") hasFulltext: Boolean = false,
        @Query("sort") sort: String? = null
    ): Response<OpenLibrarySearchResponse>


    @GET("search.json")
    suspend fun getBestsellerBooks(
        @Query("q") query: String = "bestseller",
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("fields") fields: String = "key,title,author_name,first_publish_year,cover_i",
        @Query("mode") mode: String = "bestseller",
        @Query("has_fulltext") hasFulltext: Boolean = false,
        @Query("sort") sort: String = "readinglog"
    ): Response<OpenLibrarySearchResponse>

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
            val cacheSize = 10 * 1024 * 1024 // 10 MB cache
            val cache = Cache(File(System.getProperty("java.io.tmpdir")), cacheSize.toLong())

            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .cache(cache)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Accept", "application/json")
                        .header("Cache-Control", "public, max-age=300") // 5 minutes cache
                        .header("User-Agent", "NaivetyApp/1.0 (naivety.akul@gmail.com)")
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