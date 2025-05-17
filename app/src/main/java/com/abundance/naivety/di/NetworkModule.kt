// app/src/main/java/com/abundance/naivety/di/NetworkModule.kt
package com.abundance.naivety.di

import com.abundance.naivety.data.AppDatabase
import com.abundance.naivety.network.OpenLibraryApi
import com.abundance.naivety.repository.BrowseRepository
import com.abundance.naivety.repository.BrowseRepositoryImpl
import com.abundance.naivety.repository.ListsRepository
import com.abundance.naivety.utils.BookCache
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("Cache-Control", "public, max-age=300")
                    .header("User-Agent", "NaivetyApp/1.0 (naivety.akul@gmail.com)")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://openlibrary.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenLibraryApi(retrofit: Retrofit): OpenLibraryApi {
        return retrofit.create(OpenLibraryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBrowseRepository(
        api: OpenLibraryApi,
        bookCache: BookCache,
        listsRepository: ListsRepository
    ): BrowseRepository {
        return BrowseRepositoryImpl(api, bookCache, listsRepository)
    }

    @Provides
    @Singleton
    fun provideListsRepository(
        database: AppDatabase,
        api: OpenLibraryApi
    ): ListsRepository {
        return ListsRepository(database, api)
    }
}