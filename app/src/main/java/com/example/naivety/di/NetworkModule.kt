// app/src/main/java/com/example/naivety/di/NetworkModule.kt
package com.example.naivety.di

import com.example.naivety.data.AppDatabase
import com.example.naivety.network.OpenLibraryApi
import com.example.naivety.repository.BrowseRepository
import com.example.naivety.repository.BrowseRepositoryImpl
import com.example.naivety.repository.ListsRepository
import com.example.naivety.utils.BookCache
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
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
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
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenLibraryApi(): OpenLibraryApi {
        return OpenLibraryApi.create()
    }

    @Provides
    @Singleton
    fun provideBrowseRepository(api: OpenLibraryApi, bookCache: BookCache): BrowseRepository {
        return BrowseRepositoryImpl(api, bookCache)
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